package ninja.trek;

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
			ship.cacheChunk(ship.map);
			
			//Gdx.app.log("thread runnable", "cache chunk");
		} 
			
	}

}
