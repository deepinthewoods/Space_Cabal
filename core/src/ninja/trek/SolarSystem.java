package ninja.trek;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Planet.Type;

public class SolarSystem {

	public static final int MAX_PLANETS_PER_SYSTEM = 6;
	public static final int OTHER_BODIES_PER_SYSTEM = 9;
	public static final int MAX_MOONS_PER_PLANET = 6;
	private static final String TAG = "solarsystem";
	private static final int SUN_VARIANTS_TOTAL = 3;
	public int sunVariantID = 0;
	public Planet sun;
	public Planet[] planets;
	private Array<Planet> plan = new Array<Planet>();
	public SolarSystem(int level, int seed, GameInfo gameInfo, IntMap<Quest> questList) {
		Planet planet;
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 0, Type.INNER);
		
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 1, Type.INNER);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 2, Type.EARTH_LIKE);
		
		//planet.quests.add(basicQuests.get(0).hashCode());
		
		
		
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 3, Type.EARTH_LIKE);//Type.MARS_LIKE);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 4, Type.EARTH_LIKE);//Type.GAS_GIANT);
		plan.add(planet);
		planet = new Planet(MathUtils.random(Integer.MAX_VALUE-1), 5, Type.EARTH_LIKE);//Type.GAS_GIANT);
		plan.add(planet);
		
		int numberOfOther = OTHER_BODIES_PER_SYSTEM;
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
			//TODO enforce max_moons_PER_planet
		}
		
		planets = plan.toArray(Planet.class);
		
		for (int i = 0; i < planets.length; i++){
			
			planets[i].init();
			
			makeQuest(planets[i], gameInfo, questList);
		}
		
		//sun
		sun = new Planet(MathUtils.random(Integer.MAX_VALUE-1), -1, Type.STAR);
		sunVariantID = MathUtils.random(SUN_VARIANTS_TOTAL-1);
		sun.init();
		makeQuest(sun, gameInfo, questList);
	}
	private void makeQuest(Planet planet, GameInfo gameInfo, IntMap<Quest> questList) {
		String questName = findAQuest(planet, gameInfo, questList);
		if (questName != null) {
			planet.quests.add(questName.hashCode());
		} else {
			Gdx.app.log(TAG, "didn't add quest, " + planet.planetType);
		}
		
	}
	private String findAQuest(Planet planet, GameInfo gameInfo, IntMap<Quest> quests) {
		//IntMap<Quest> quests = gameInfo.getQuests();
		Iterator<Entry<Quest>> iter = quests.iterator();
		QuestArray validQuests = Pools.obtain(QuestArray.class);
		//Gdx.app.log(TAG, "find a quest " + quests.size);

		Quest quest = null;
		while (iter.hasNext()){
			Entry<Quest> e = iter.next();
			quest = e.value;
			//Gdx.app.log(TAG, "look at quest first" + quest.name);
			if (quest.isValidFor(planet)) {
				validQuests.add(quest);
				//Gdx.app.log(TAG, "add valid" + quest.name);
			} else {
				//Gdx.app.log(TAG, "not valid " + quest.name);
			}
		}
		boolean found = false;
		if (validQuests.size == 0) return null;
		while (!found){
			quest = validQuests.get(MathUtils.random(validQuests.size-1));
			//Gdx.app.log(TAG, "looking at valid " + quest.name);
			if (quest.isOneOff) {
				if (gameInfo.hasSpawnedQuest(quest)) continue;
				gameInfo.onSpawnQuest(quest);
				//Gdx.app.log(TAG, "spawn quest " + quest.name + quest.name.hashCode());

				return quest.name;
			}
			//Gdx.app.log(TAG, "spawn quest " + quest.name + quest.name.hashCode());

			gameInfo.onSpawnQuest(quest);
			return quest.name;
		}
		
		return null;
	}

}
