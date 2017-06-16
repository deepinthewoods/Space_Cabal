package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship.EntityArray;
import ninja.trek.ui.UI;

public class World {
	private static final String TAG = "world";
	private Array<Ship> maps = new Array<Ship>(true, 4);
	private FontManager fonts;
	private float accum = 0f;
	private final float timeStep = 1f/60f;
	
	public World(FontManager fontManager) {
		
		fonts = fontManager; 
		

	}
	public void update(SpriteBatch batch, Camera camera, World world){
		
		accum += Gdx.graphics.getDeltaTime();
		while (accum > timeStep){
			accum -= timeStep;
			for (Ship map : maps)
				map.updateBlocks();
			for (Ship map : maps)
				map.updateEntities(world);
			
		}
		
		for (Ship map : maps)
			map.updateDraw(batch);
		
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera, ShapeRenderer shape, UI ui){
		
		batch.disableBlending();
		for (Ship map : maps) map.draw(batch, camera, this);
		batch.enableBlending();
		
		batch.begin();
		for (Ship map : maps) map.drawEntities(batch);
		batch.end();
		for (Ship map : maps) map.drawLines(shape, ui);
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
		EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
		//EntityArray entities = Pools.obtain(EntityArray.class);
		maps.get(1).load(map, entities);
		Data.jsonPool.free(json);
		
		for (int i = 0; i < maps.size; i++){
			maps.get(i).categorizeSystems();
			boolean hasShipE = false;
			for (int ce = entities.size - 1; ce > 0; ce--){
				Entity e = entities.get(ce);
				if (e instanceof ShipEntity){
					if (hasShipE) entities.removeValue(e, true);
					hasShipE = true;
				} 
			}
			if (!hasShipE ){
				
			}
			ShipEntity shipE = Pools.obtain(ShipEntity.class);
			shipE.setDefaultAI();
			maps.get(i).addEntity(shipE );
		}
		Entity e = Pools.obtain(Entity.class);
		e.pos(maps.get(0).map.spawn);
		e.setDefaultAI();
		maps.get(0).addEntity(e);
	}
	public Ship getEnemyShip() {
		return maps.get(1);
	}
	
}
