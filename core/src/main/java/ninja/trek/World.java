package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship.Alignment;
import ninja.trek.Ship.EntityArray;

public class World {
	private static final String TAG = "world";
	private Array<Ship> maps = new Array<Ship>(true, 4);
	private FontManager fonts;
	private float accum = 0f;
	public final static float timeStep = 1f/60f;
	
	public World(FontManager fontManager) {
		
		fonts = fontManager; 
		

	}
	public void update(SpriteBatch batch, Camera camera, World world, UI ui){
		
		accum += Gdx.graphics.getDeltaTime();
		while (accum > timeStep){
			accum -= timeStep;
			for (Ship map : maps)
				map.updateBlocks();
			//for (Ship map : maps)
			for (int i = 0; i < maps.size; i++)	
				maps.get(i).updateEntities(world, ui);
			
		}
		
		for (Ship map : maps)
			map.updateDraw(batch);
		
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera, ShapeRenderer shape, UI ui){
		
		batch.disableBlending();
		for (Ship map : maps) map.draw(batch, camera, this);
		batch.enableBlending();
		
		batch.begin();
		for (int i = 0; i < maps.size; i++){
			Ship map = maps.get(i);
			map.drawEntities(batch, this);
		}
//		for (Ship map : maps) 
		batch.end();
		for (int i = 0; i < maps.size; i++){
			Ship map = maps.get(i);
			map.drawLines(shape, ui, targettingIndex != -1, camera);
			if (map.alignment == Alignment.TOP_RIGHT)
				map.drawTargettedLines(shape, ui, getPlayerShip());
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
		Json json = Data.jsonPool.obtain();
		FileHandle f = Gdx.files.external(Main.SHIP_SAVE_LOCATION + "test" + "." + Main.MAP_FILE_EXTENSION);
		IntPixelMap map = json.fromJson(IntPixelMap.class, f.readString());
		FileHandle entityFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.ENTITY_FILE_EXTENSION);
		FileHandle hullFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.MAP_HULL_EXTENSION);
		EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
		//EntityArray entities = Pools.obtain(EntityArray.class);
		Texture hull = null;
		if (hullFile.exists())
			hull = new Texture(hullFile);
		maps.get(1).load(map, entities, hull);
		Data.jsonPool.free(json);
		
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
	
	
	public void startNewGameMenu() {
		Json json = Data.jsonPool.obtain();
		FileHandle f = Gdx.files.external(Main.SHIP_SAVE_LOCATION + "test" + "." + Main.MAP_FILE_EXTENSION);
		IntPixelMap map = json.fromJson(IntPixelMap.class, f.readString());
		FileHandle entityFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.ENTITY_FILE_EXTENSION);
		FileHandle hullFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.MAP_HULL_EXTENSION);
		EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
		//EntityArray entities = Pools.obtain(EntityArray.class);
		Texture hull = null;
		if (hullFile.exists())
			hull = new Texture(hullFile);
		maps.get(1).load(map, entities, hull);
		Data.jsonPool.free(json);
		
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
}
