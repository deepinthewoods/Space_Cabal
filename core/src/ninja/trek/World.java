package ninja.trek;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship.Alignment;
import ninja.trek.Ship.EntityArray;
import ninja.trek.actions.ADrone;
import ninja.trek.entity.Entity;
import ninja.trek.entity.Missile;
import ninja.trek.entity.ShipEntity;

public class World {
	private static final String TAG = "world";
	public final SolarSystemGraph solarSystemGraph;
	private final ShipFcatory shipFactory;
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
	private Runnable[] runnable;
	public FrameBuffer colorIndexBuffer;
	ShaderProgram colorIndexShader;
	public final static float timeStep = 1f/60f;
	public IndexedAStarPathFinder<PlanetNode> universePath;
	private float warpShipZoom;

	public World(FontManager fontManager, ShaderProgram shader, Sprite pixelSprite, PlanetRenderer planet, ModelBatch modelBatch, ShipFcatory shipFactory) {
		this.modelBatch = modelBatch;
		this.planet = planet;
		this.shader = shader;
		this.pixelSprite = pixelSprite;
		fonts = fontManager;
		this.shipFactory = shipFactory;
		solarSystemGraph = new SolarSystemGraph();
		universePath = new IndexedAStarPathFinder<PlanetNode>(solarSystemGraph
		);

		
		vertsForThreads = new float[MainSpaceCabal.THREADS][Ship.CHUNKSIZE * Ship.CHUNKSIZE * 4 * 3];
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

		runnable = new Runnable[3];
		threads = new PauseableThread[runnable.length];
		for (int i = 0; i < runnable.length; i++){
			runnable[i] = new MapCacheRunnable(i, this);
			threads[i] = new PauseableThread(runnable[i]);
			if (Gdx.app.getType() == ApplicationType.Android) {
				threads[i].start();
			}
		}

		
		colorIndexBuffer = new FrameBuffer(Format.RGBA8888, 128, 32, false);
		
		colorIndexShader = new ShaderProgram(Gdx.files.internal("colorIndex.vert"), Gdx.files.internal("colorIndex.frag"));
		
		if (!colorIndexShader.isCompiled()) {
			throw new GdxRuntimeException("index shader error " + colorIndexShader.getLog());
		}
		colorIndexBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
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
	public void update(SpriteBatch batch, Camera camera, UI ui, BackgroundRenderer background, PlanetRenderer planet, Stage stage){
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			for (int i = 0; i < runnable.length; i++){
				runnable[i].run();
			}
		}
		accum += Gdx.graphics.getDeltaTime();
		if (warpingToSolarSystemMap){
			warpAlpha += Gdx.graphics.getDeltaTime();
			planet.setAlpha(warpAlpha);
			setAlpha(warpAlpha);
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
				showQuestScreen(ui, stage);
				Ship ship = getPlayerShip();
				ui.set(ship);
				
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
				maps.get(i).updateEntities(this, ui);
			
		}
		
		for (Ship map : maps)
			map.updateDraw(mesh, cacheShader);
		
	}

	private void setAlpha(float a) {
		float zoom = Interpolation.circle.apply( warpShipZoom, 15f,  a);

		getPlayerShip().camera.zoom = zoom;
		getPlayerShip().camera.update();
	}

	private void showQuestScreen(UI ui, Stage stage) {
		ui.showQuestScreen(info, stage, getPlayerShip(), this);
		planet.sunFromSide = false;
	}
	boolean hasMadeIndexPNG = false;
	public void draw(SpriteBatch batch, OrthographicCamera camera, ShapeRenderer shape, UI ui, boolean paused){
		
		colorIndexBuffer.begin();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, colorIndexBuffer.getWidth(), colorIndexBuffer.getHeight());
		batch.setShader(colorIndexShader);
		batch.begin();
		colorIndexShader.setUniformf("u_time", MathUtils.random(10000));
		batch.draw(pixelSprite.getTexture(), 0, 0, colorIndexBuffer.getWidth(), colorIndexBuffer.getHeight());
		batch.end();
		colorIndexBuffer.end();
		if (!hasMadeIndexPNG) {
			//hasMadeIndexPNG = true;
			//Pixmap pix;
			//Texture tex = colorIndexBuffer.getColorBufferTexture();
			//if (!tex.getTextureData().isPrepared()) tex.getTextureData().prepare();
			//pix = tex.getTextureData().consumePixmap();
			//PixmapIO.writePNG(Gdx.files.external("screen.png"), pix);
		}
		
		if (planet.sunFromSide || (!warpingBetweenPlanets  && warpAlpha < .99f//&& !warpingToPlanet && !warpingToSolarSystemMap
				&& !planetSelectOn
		))
		for (int i = 0; i < maps.size; i++){
			Ship map = maps.get(i);
			map.updateCamera(camera, this, i, warpingToSolarSystemMap | warpingToPlanet);
			map.enableScissor(this);
			batch.disableBlending();
			
			map.draw(batch, camera, this, paused, colorIndexBuffer.getColorBufferTexture(), mesh, cacheShader, warpingToPlanet || warpingToSolarSystemMap, i);
			batch.setProjectionMatrix(map.camera.combined);
			batch.enableBlending();
			batch.setShader(null);
			batch.begin();
			map.drawEntities(batch, this, warpingToPlanet || warpingToSolarSystemMap, i == 0);
			batch.end();
			batch.begin();
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
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 10, 10);
        batch.begin();

       // batch.draw(colorIndexBuffer.getColorBufferTexture(), 0, 0, 10, 10);//TODO
        batch.end();
	}
	
	public void addMap(Ship map){
		maps.add(map);
		Gdx.app.log(TAG, "addMap " + maps.size);
	}
	public void clearDrones(){
		while (maps.size > 2)Pools.free(maps.removeIndex(maps.size-1));
	}
	public void addDrone(String name, Ship parentShip){
		Drone droneShip = Pools.obtain(Drone.class);
		loadShip(name, droneShip);
		droneShip.alignment = parentShip.alignment;
		droneShip.parent = parentShip;
		droneShip.setRedrawFill();
		//droneShip.removeEntity(droneShip.getShipEntity());
		ADrone aDrone = Pools.obtain(ADrone.class);
		droneShip.getShipEntity().getAI().addToEnd(aDrone);
		addMap(droneShip);
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

		
	}
	
	String[] playerShipNames = {"test", "abasic"};

	private boolean isNewGameMenu = false;
	private Ship oldShip;
	private int currentNewGameShipIndex;
	private GameInfo info;
	private boolean warpingToSolarSystemMap, warpingToPlanet, warpingBetweenPlanets;
	
	public void startNewGameMenu() {

		currentNewGameShipIndex = 0;
		loadShipForNew(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());
		//warpingToSolarSystemMap = true;
		getPlayerShip().setForNewGamePreview();

	}
	
	public void showNextNewGameShip(){
		currentNewGameShipIndex++;
		if (currentNewGameShipIndex > playerShipNames.length-1)
			currentNewGameShipIndex = 0;
		loadShipForNew(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());
		getPlayerShip().setForNewGamePreview();
		getPlayerShip().setRedrawMap();

	}
	
	public void showPrevNewGameShip(){
		currentNewGameShipIndex--;
		if (currentNewGameShipIndex < 0)
			currentNewGameShipIndex = playerShipNames.length-1;
		loadShipForNew(playerShipNames[currentNewGameShipIndex], getPlayerShip());
		getPlayerShip().removeEntity(getPlayerShip().getShipEntity());
		getPlayerShip().setForNewGamePreview();
		getPlayerShip().setRedrawMap();
	}
	
	public GameInfo startNewGame(){
		//startTestBattle();
		MathUtils.random.setSeed(System.currentTimeMillis());
		GameInfo info = new GameInfo(MathUtils.random(Integer.MAX_VALUE-1));
		this.info = info;
		warpAlpha = 0f;
		warpingToPlanet = true;
		planet.setInfo(info);
		for (int i = 0; i < maps.size; i++){
			Ship m = maps.get(i);
			m.categorizeSystems();
			m.calculateConnectivity(this);
			if (!m.hasShipEntity()){
				ShipEntity shipE = Pools.obtain(ShipEntity.class);
				shipE.setDefaultAI();
				maps.get(i).addEntity(shipE );
			}
			//Gdx.app.log(TAG, "size " + i + "  " + maps.get(i).getEntities().size);
		}
		info.currentPlanet = -1;
		planet.selectedPlanet = -1;
		planet.sunFromSide = true;
		return info;
	}

	public void showSolarSystemView(UI ui){
		ui.set(null);
		ui.setEntity(null);
		warpAlpha = 0f;
		warpingToSolarSystemMap = true;
		warpShipZoom = getPlayerShip().camera.zoom;
		//MainSpaceCabal.paused = true;
	}
	
	public void loadShipForNew(String name, Ship ship){
		
		FileHandle f = Gdx.files.internal(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
		Gdx.app.log(TAG, "load ship " + f.file().getAbsolutePath());
		loadShip(f, ship);
	}
	public void loadShip(String name, Ship ship){
		
		FileHandle f = Gdx.files.external(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
		if (!f.exists())
			f = Gdx.files.internal(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
		if (!f.exists())
			throw new GdxRuntimeException("shpi file not found " + name);

		loadShip(f, ship);
	}
	public void loadShip(FileHandle f, Ship ship){
		
		Json json = Data.jsonPool.obtain();
		IntPixelMap map = json.fromJson(IntPixelMap.class, f.readString());
		FileHandle entityFile = f.sibling(f.nameWithoutExtension() + "." + MainSpaceCabal.ENTITY_FILE_EXTENSION);
		FileHandle hullFile = f.sibling(f.nameWithoutExtension() + "." + MainSpaceCabal.MAP_HULL_EXTENSION);
		FileHandle invFile = f.sibling(f.nameWithoutExtension() + "." + MainSpaceCabal.MAP_INVENTORY_FILE_EXTENSION);
		FileHandle mapBlocksFile = f.sibling(f.nameWithoutExtension() + "." + MainSpaceCabal.MAP_BLOCKS_FILE_EXTENSION);
		EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
		//for (Entity e : entities)
			//Gdx.app.log(TAG, "ENTITY " + e);

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
		
		IntArray inv = json.fromJson(IntArray.class, invFile.readString());
		
		ship.load(map, entities, hull, inv);
		
		Data.jsonPool.free(json);
		ship.categorizeSystems();
		ship.calculateConnectivity(this);
		if (!ship.hasShipEntity()){
			ShipEntity shipE = Pools.obtain(ShipEntity.class);
			shipE.setDefaultAI();
			ship.addEntity(shipE );
		}
		Gdx.app.log(TAG, "LOAD " + ship.mapWidth + "  " + ship.getEntities().size);
	
	}
	public Ship getEnemyShip() {
		return maps.get(1);
	}

	public void cancelTarget() {
		getPlayerShip().cancelWeaponTarget(targettingIndex);
		getEnemyShip().zoomOutForTarget();
		targettingIndex = -1;
	}
	public static String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};


	public void switchToShip(Entity miss, Ship to) {
		//Ship enemyShip = getEnemy(miss.ship);
		miss.ship.removeEntityNoPool(miss);
		to.addEntity(miss);
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
