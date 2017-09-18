package ninja.trek;

import com.badlogic.gdx.math.MathUtils;

public class MapCacheRunnable implements Runnable {
	
	private int index;
	private World world;
	public MapCacheRunnable(int index, World world) {
		this.index = index;
		this.world = world;
	}
	@Override
	public void run() {
		Ship ship = world.getShipForThread(index);
		if (ship != null) {
			//if (MathUtils.randomBoolean())
			//	ship.cacheChunk(ship.fill, ship.cacheVertsFill);
			//else
				ship.cacheChunk(ship.map, ship.cacheVerts);
			
			//Gdx.app.log("thread runnable", "cache chunk");
		} 
			
	}

}
