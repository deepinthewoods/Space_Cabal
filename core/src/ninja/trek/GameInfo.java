package ninja.trek;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

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
	private IntMap<Quest> quests = new IntMap<Quest>();

	
	public void createGalaxy(){
		Array<Quest> basicQuests = new Array<Quest>();
		Array<Quest> specialQuests = new Array<Quest>();
		Quest quest = new Quest();
		quest.name = "quest name";
		quest.options.add(new QuestOption("option A"));
		quest.options.add(new QuestOption("option b"));
		quests .put(quest.hashCode(), quest);
		basicQuests.add(quest);
		
		MathUtils.random.setSeed(seed);
		for (int i = 0; i < systems.length; i++){
			systems[i] = new SolarSystem(i, MathUtils.random(Integer.MAX_VALUE-1), basicQuests, specialQuests);
		}
		
		
	}


	public boolean hasCompleted(int questHash, int systemIndex, int planetIndex) {
		return systems[systemIndex].planets[planetIndex].completed.contains(questHash);
	}


	public boolean isValid(int questHash, Ship ship) {
		quests.get(questHash);
		return true;
	}


	public Quest getQuest(int questHash) {
		
		return quests.get(questHash, null);
	}


	public Quest getQuest(String string) {
		return getQuest(string.hashCode());
	}
}
