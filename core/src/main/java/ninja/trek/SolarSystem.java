package ninja.trek;

import com.badlogic.gdx.math.MathUtils;

public class SolarSystem {

	public static final int MAX_PLANETS_PER_SYSTEM = 8;
	public int sunVariantID = 0;
	public Planet[] planets;
	public SolarSystem(int level, int seed) {
		planets = new Planet[MAX_PLANETS_PER_SYSTEM];
		for (int i = 0; i < MAX_PLANETS_PER_SYSTEM; i++){
			planets[i] = new Planet(MathUtils.random(Integer.MAX_VALUE-1), i);
		}
	}

}
