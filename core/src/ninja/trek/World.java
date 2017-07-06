package ninja.trek;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship.Alignment;
import ninja.trek.Ship.EntityArray;

public class World {
	private static final String TAG = "world";
	private Array<Ship> maps = new Array<Ship>(true, 4);
	private FontManager fonts;
	private float accum = 0f;
	private ShaderProgram shader;
	private Sprite pixelSprite;
	private float warpAlpha, warpBeta;
	public boolean planetSelectOn;
	private PlanetRenderer planet;
	private boolean betaPhaseTwo;
	private ModelBatch modelBatch;
	private float[][] vertsForThreads;
	private short[] indicesForChunkMesh;
	private Mesh mesh;
	private ShaderProgram cacheShader;
	private PauseableThread[] threads;
	public final static float timeStep = 1f/60f;
	
	public World(FontManager fontManager, ShaderProgram shader, Sprite pixelSprite, PlanetRenderer planet, ModelBatch modelBatch) {
		this.modelBatch = modelBatch;
		this.planet = planet;
		this.shader = shader;
		this.pixelSprite = pixelSprite;
		fonts = fontManager; 
		
		vertsForThreads = new float[Main.THREADS][Ship.CHUNKSIZE * Ship.CHUNKSIZE * 4 * 3];
		indicesForChunkMesh = new short[Ship.CHUNKSIZE * Ship.CHUNKSIZE * 6];
		//for (int i = 0; i < vertsForThreads.length; i++)
		mesh = new Mesh(true, vertsForThreads[0].length , indicesForChunkMesh.length, 
				new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE)
				, VertexAttribute.ColorPacked()
				//, VertexAttribute.TexCoords(0)
				//, VertexAttribute.Normal()
				);
		short index = 0;
		for (int i = 0; i < indicesForChunkMesh.length;) {
			indicesForChunkMesh[i++] = index;
			indicesForChunkMesh[i++] = (short) (index+1);
			indicesForChunkMesh[i++] = (short) (index+2);
			indicesForChunkMesh[i++] = (short) (index+1);
			indicesForChunkMesh[i++] = (short) (index+2);
			indicesForChunkMesh[i++] = (short) (index+3);			
			index += 4;
		}
		mesh.setIndices(indicesForChunkMesh);
		cacheShader = createDefaultShader();

		threads = new PauseableThread[2];
		threads[0] = new PauseableThread(new MapCacheRunnable(0, this));
		//threads[1] = new PauseableThread(new MapCacheRunnable(1, this));
		threads[0].start();
		//threads[1].start();
	}
	public ShaderProgram createDefaultShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			//+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			//+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			//+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			//+ "varying vec2 v_texCoords;\n" //
			//+ "uniform sampler2D u_texture;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  gl_FragColor = v_color;\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}
	public void update(SpriteBatch batch, Camera camera, World world, UI ui, BackgroundRenderer background, PlanetRenderer planet, Stage stage	){
		
		accum += Gdx.graphics.getDeltaTime();
		if (warpingToSolarSystemMap){
			warpAlpha += Gdx.graphics.getDeltaTime();
			planet.setAlpha(warpAlpha);
			if (warpAlpha > 1f){
				warpingToSolarSystemMap = false;
				planetSelectOn = true;
				warpAlpha = 0f;
				ui.addSolarSystemWindow(stage);
			}
		}
		if (warpingToPlanet){

			warpAlpha += Gdx.graphics.getDeltaTime();
			planet.setAlpha(1f - warpAlpha);
			
			if (warpAlpha > 1f){
				warpingToPlanet = false;
				warpAlpha = 0f;
				planetSelectOn = false;
			}
		
		}
		if (warpingBetweenPlanets){

			warpBeta += Gdx.graphics.getDeltaTime();
			background.setAlpha(warpBeta<1f?warpBeta:2f - warpBeta);
			if (warpBeta > 1.75f){
				if (!betaPhaseTwo){
					betaPhaseTwo = true;
					planet.unSelect();
				}
			} else 
				betaPhaseTwo = false;
			if (warpBeta > 2f){
				warpingBetweenPlanets = false;
				warpBeta = 0f;
				warpAlpha = 0f;;
				warpingToPlanet = true;
				
			}
		
		}
		while (accum > timeStep){
			accum -= timeStep;
			for (Ship map : maps)
				map.updateBlocks();
			//for (Ship map : maps)
			for (int i = 0; i < maps.size; i++)	
				maps.get(i).updateEntities(world, ui);
			
		}
		
		for (Ship map : maps)
			map.updateDraw(mesh, cacheShader);
		
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera, ShapeRenderer shape, UI ui){
		
		
		
		 
		
		if (!warpingBetweenPlanets && !warpingToPlanet && !warpingToSolarSystemMap && !planetSelectOn)
		for (int i = 0; i < maps.size; i++){
			Ship map = maps.get(i);
			map.enableScissor(this);
			batch.disableBlending() ;
			
			map.draw(batch, camera, this);
			batch.begin();
			batch.enableBlending();
			map.drawEntities(batch, this);
			map.drawLines(shape, ui, targettingIndex != -1, camera, this);
			if (map.alignment == Alignment.TOP_RIGHT)
				map.drawTargettedLines(shape, ui, getPlayerShip());
			batch.end();
			
		}
//		for (Ship map : maps) 
		Ship.disableScissor();
		for (int i = 0; i < maps.size; i++){
			Ship map = maps.get(i);
			
			
		}
	}
	
	public void addMap(Ship map){
		maps.add(map);
	}
	public Ship getMapForScreenPosition(int x, int y) {
		int closestDist = 1000070;
		Ship closest = null;
		for (Ship map : maps){
			v.set(x, y, 0);
			map.camera.unproject(v);
			if (v.x > 0 && v.y > 0 && v.x < map.mapWidth && v.y < map.mapHeight) return map;
			v2.set(map.mapWidth/2, map.mapHeight/2, 0);
			
			map.camera.project(v2);
			v2.sub(x, y, 0);
			int d = (int) v2.len();//(int) Math.abs(Math.min(v.x, v.y) * map.camera.zoom);
			//Gdx.app.log(TAG, "dist " + d);
			
			if (d < closestDist){
				closestDist = d;
				closest = map;
			}
		}
		
		return closest;
	}
	private Vector3 v = new Vector3(), v2 = new Vector3();
	public int targettingIndex = -1;

	public Ship getPlayerShip() {
		
		return maps.get(0);
	}
	public Ship getMapForDragging(int x, int y) {
		
		return maps.get(0);
	}
	public void startTestBattle() {
		loadShip("test", maps.get(1));
		
		
		//Gdx.app.log(TAG, "IT " + maps.size);
		for (int i = 0; i < maps.size; i++){
			Ship m = maps.get(i);
			m.categorizeSystems();
			
			
			if (!m.hasShipEntity()){
				ShipEntity shipE = Pools.obtain(ShipEntity.class);
				shipE.setDefaultAI();
				maps.get(i).addEntity(shipE );
			}
			Gdx.app.log(TAG, "size " + i + "  " + maps.get(i).getEntities().size);
		}
		for (int i = 0; i < 1; i++){
			Entity e = Pools.obtain(Entity.class);
			e.glyph = letters[i % letters.length];
			e.pos(maps.get(0).map.spawn);
			e.setDefaultAI();
			maps.get(0).addEntity(e);
			
		}
		
	}
	
	String[] playerShipNames = {"test", "abasic"};
	Array<Ship> playerShips;
	Array<String> queuedPlayerShips = new Array<String>();
	private boolean isNewGameMenu = false;
	private Ship oldShip;
	private int currentNewGameShipIndex;
	private GameInfo info;
	private boolean warpingToSolarSystemMap, warpingToPlanet, warpingBetweenPlanets;
	
	public void startNewGameMenu() {
		/*if (playerShips == null){
			playerShips = new Array<Ship>();
			for (String name : playerShipNames){
				Ship ship = new Ship(new IntPixelMap(16, 16), 1, pixelSprite, fonts, shader);
				loadShip(name, ship);	
				playerShips.add(ship);
			}
		} else {
			for (String name : queuedPlayerShips){
				Ship ship = new Ship(new IntPixelMap(16, 16), 1, pixelSprite, fonts, shader);
				loadShip(name, ship);	
				playerShips.add(ship);
			}
			
		}
		
		Ship plShip = playerShips.get(0);
		isNewGameMenu  = true;
		oldShip = maps.get(0);
		maps.set(0, plShip);;*/
		currentNewGameShipIndex = 0;
		loadShip(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());
		//warpingToSolarSystemMap = true;
		
	}
	
	public void showNextNewGameShip(){
		currentNewGameShipIndex++;
		if (currentNewGameShipIndex > playerShipNames.length-1)
			currentNewGameShipIndex = 0;
		loadShip(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());

	}
	
	public void showPrevNewGameShip(){
		currentNewGameShipIndex--;
		if (currentNewGameShipIndex < 0)
			currentNewGameShipIndex = playerShipNames.length-1;
		loadShip(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());
	}
	
	public GameInfo startNewGame(){
		//startTestBattle();
		MathUtils.random.setSeed(System.currentTimeMillis());
		GameInfo info = new GameInfo(MathUtils.random(Integer.MAX_VALUE-1));
		this.info = info;
		warpAlpha = 1f;
		warpingToSolarSystemMap = true;
		planet.setInfo(info);
		return info;
	}

	public void showSolarSystemView(){
		warpAlpha = 0f;
		warpingToSolarSystemMap = true;
	}
	
	public void loadShip(String name, Ship ship){
		
		FileHandle f = Gdx.files.external(Main.SHIP_SAVE_LOCATION + name + "." + Main.MAP_FILE_EXTENSION);
		loadShip(f, ship);
	}
	public void loadShip(FileHandle f, Ship ship){
		
		Json json = Data.jsonPool.obtain();
		IntPixelMap map = json.fromJson(IntPixelMap.class, f.readString());
		FileHandle entityFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.ENTITY_FILE_EXTENSION);
		FileHandle hullFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.MAP_HULL_EXTENSION);
		FileHandle mapBlocksFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.MAP_BLOCKS_FILE_EXTENSION);
		EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
		Texture hull = null;
		if (hullFile.exists())
			hull = new Texture(hullFile);
		
		try {
			ObjectInputStream blockStream = new ObjectInputStream(new GZIPInputStream(mapBlocksFile.read()));
			int[] blocks = (int[]) blockStream.readObject();
			map.setRawBlocks(blocks);
			blockStream.close();
		} catch (ClassNotFoundException | IOException e) {
			Gdx.app.log(TAG, "failed to load raw blocks");
			e.printStackTrace();
		}
		
		ship.load(map, entities, hull);
		
		Data.jsonPool.free(json);
		ship.categorizeSystems();
		if (!ship.hasShipEntity()){
			ShipEntity shipE = Pools.obtain(ShipEntity.class);
			shipE.setDefaultAI();
			ship.addEntity(shipE );
		}
		//Gdx.app.log(TAG, "size " + i + "  " + maps.get(i).getEntities().size);
	
	}
	public Ship getEnemyShip() {
		return maps.get(1);
	}
	public Ship getEnemy(Ship map) {
		for (Ship ship : maps){
			if (ship != map) return ship;
		}
		return null;
	}
	public void cancelTarget() {
		getPlayerShip().cancelWeaponTarget(targettingIndex);
		targettingIndex = -1;
	}
	private static String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

	public void switchToEnemyShip(Missile miss) {
		Ship enemyShip = getEnemy(miss.ship);
		miss.ship.removeEntityNoPool(miss);
		enemyShip.addEntity(miss);
	}
	public void dispose() {
		for (int i = 0; i < maps.size; i++)
			maps.get(i).dispose();
		
	}
	public void goToOrbit(int orbitType) {
		int planetI = planet.selectedPlanet;
		info.currentPlanet = planetI;
		info.currentOrbitalDepth = orbitType;
		warpBeta = 0f;
		warpingBetweenPlanets = true;
		
	}
	public Ship getShipForThread(int index) {
		if (maps.size <= index) return null;
		return maps.get(index);
	}
}
