package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.QuestOption.QuestCommand;
import ninja.trek.QuestOption.QuestRequirement;

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
	private static final String TAG = "Game Info";

	public int xp;
	public transient SolarSystem[] systems = new SolarSystem[8];
	private IntMap<Quest> quests = new IntMap<Quest>();

	
	public void createGalaxy(){
		Array<Quest> basicQuests = new Array<Quest>();
		Array<Quest> specialQuests = new Array<Quest>();
		Quest quest = new Quest();
		quest.name = "quest name";
		QuestOption qOption = new QuestOption("option A");
		qOption.requireAll.add(QuestRequirement.HAS_ENGINES);
		qOption.requireAll.add(QuestRequirement.HAS_ENGINES);
		qOption.commands = new String[] {"hostile"};
		qOption.next = new String[] {"nextquest"};
		quest.options.add(qOption);
		quest.options.add(new QuestOption("option b"));
		quests.put(quest.hashCode(), quest);
		basicQuests.add(quest);
		quest.text = "you come across a war ship";
		quest.tags = new String[]{"easy", "fight"};
		quest.commands = new String[]{"spawn snail"};
		
		Quest nextQ = new Quest();
		nextQ.name = "nextquest";
		nextQ.text = "this is another quest";
		QuestOption nextO = new QuestOption("exit");
		nextQ.options.add(nextO);
		quests.put(nextQ.hashCode(), nextQ);
		
		Json json = Pools.obtain(Json.class);
		String s = json.prettyPrint(basicQuests);
	
		Gdx.app.log(TAG, s);
		Pools.free(json);
		MathUtils.random.setSeed(seed);
		for (int i = 0; i < systems.length; i++){
			systems[i] = new SolarSystem(i, MathUtils.random(Integer.MAX_VALUE-1), basicQuests, specialQuests);
		}
		
		
	}


	public boolean hasCompleted(int questHash, int systemIndex, int planetIndex) {
		if (true) return false;
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
		if (string == null) return null;
		return getQuest(string.hashCode());
	}
}
