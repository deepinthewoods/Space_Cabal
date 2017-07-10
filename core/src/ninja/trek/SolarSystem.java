package ninja.trek;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import ninja.trek.Planet.Type;

public class SolarSystem {

	public static final int MAX_PLANETS_PER_SYSTEM = 6;
	public static final int MAX_OTHER_BODIES_PER_SYSTEM = 9;
	public int sunVariantID = 0;
	public Planet[] planets;
	private Array<Planet> plan = new Array<Planet>();
	public SolarSystem(int level, int seed, Array<Quest> basicQuests, Array<Quest> specialQuests) {
		Planet planet;
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 0, Type.INNER);
		
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 1, Type.INNER);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 2, Type.EARTH_LIKE);
		
		planet.quests.add(basicQuests.get(0).hashCode());
		
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 3, Type.MARS_LIKE);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 4, Type.GAS_GIANT);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 5, Type.GAS_GIANT);
		plan.add(planet);
				
		
		
		int numberOfOther = MAX_OTHER_BODIES_PER_SYSTEM;
		int[] totalChildren = new int[MAX_PLANETS_PER_SYSTEM];
		for (int i = 0; i < numberOfOther ; i++){
			Planet moon;
			if (MathUtils.randomBoolean())
				moon = new Planet(MathUtils.random(Integer.MAX_VALUE-1), i, Type.MOON);
			else
			moon = new Planet(MathUtils.random(Integer.MAX_VALUE-1), i, Type.METEOR);
			moon.parent = MathUtils.random(MAX_PLANETS_PER_SYSTEM-1);
			moon.parentOrder = totalChildren[moon.parent]++;
			plan.add(moon);
		}
		
		planets = plan.toArray(Planet.class);
		
		for (int i = 0; i < planets.length; i++){
			planets[i].init();
		}
	}

}
