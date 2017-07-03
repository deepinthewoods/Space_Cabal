package ninja.trek;

import com.badlogic.gdx.math.MathUtils;

public class GameInfo {

	public GameInfo(int seed) {
		this.seed = seed;
		createGalaxy();
	}
	public final int seed;
	public int currentLevel;
	public int currentSystem;
	public int currentPlanet = 7;
	public int currentOrbitalDepth = 1;
	public static final int ORBIT_LANDED = 0, ORBIT_ORBIT = 1, ORBIT_ELLIPTICAL = 2;

	public int xp;
	public transient SolarSystem[] systems = new SolarSystem[8];
	
	
	public void createGalaxy(){
		MathUtils.random.setSeed(seed);
		for (int i = 0; i < systems.length; i++){
			systems[i] = new SolarSystem(i, MathUtils.random(Integer.MAX_VALUE-1));
		}
		
	}
}
