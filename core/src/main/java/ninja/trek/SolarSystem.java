package ninja.trek;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class SolarSystem {

	public static final int MAX_PLANETS_PER_SYSTEM = 6;
	public static final int MAX_OTHER_BODIES_PER_SYSTEM = 9;
	public int sunVariantID = 0;
	public Planet[] planets;
	private Array<Planet> plan = new Array<Planet>();
	public SolarSystem(int level, int seed) {
		for (int i = 0; i < MAX_PLANETS_PER_SYSTEM; i++){
			plan.add(new Planet(MathUtils.random(Integer.MAX_VALUE-1), i));
		}
		int numberOfOther = MAX_OTHER_BODIES_PER_SYSTEM;
		int[] totalChildren = new int[MAX_PLANETS_PER_SYSTEM];
		for (int i = 0; i < numberOfOther ; i++){
			Planet moon = new Planet(MathUtils.random(Integer.MAX_VALUE-1), i);
			moon.parent = MathUtils.random(MAX_PLANETS_PER_SYSTEM-1);
			moon.parentOrder = totalChildren[moon.parent]++;
			plan.add(moon);
		}
		
		planets = plan.toArray(Planet.class);
		
	}

}
