package ninja.trek.gen;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.MainSpaceCabal;
import ninja.trek.PlanetNode;
import ninja.trek.Quest;
import ninja.trek.QuestArray;
import ninja.trek.Ship;

public class GameInfo {



	public GameInfo(int seed) {
		this.seed = seed;
		
		IntMap<Quest> questList = getQuestList();
		quests = questList;
		//IntMap<Quest> questList = null;//getQuestList();
		
		String[] basic = {};
		String[] special = {};//names of all quests
		createGalaxy(questList);
		
	}
	
	
	public static IntMap<Quest> getQuestList() {
		Json json = Pools.obtain(Json.class);
		FileHandle file = Gdx.files.internal(MainSpaceCabal.QUEST_SAVE_LOCATION + "allquests.json");
	
		IntMap<Quest> q = new IntMap<Quest>(); 
		QuestArray qArr = json.fromJson(QuestArray.class, file);
		Iterator<Quest> iter = qArr.iterator();
		while (iter.hasNext()) {
			Quest val = iter.next();
			if (q.containsKey(val.hashCode()))
				throw new GdxRuntimeException("duplicate name hashes " + val.name + " and " + q.get(val.hashCode()).name);
			q.put(val.hashCode(), val);
		}
		
		
		Pools.free(json);
		Gdx.app.log(TAG, "load quests " + q);
		return q ;
	}
	
	public static void packQuests() {
		Json json = Pools.obtain(Json.class);
		QuestArray all = new QuestArray();
		FileHandle folder = Gdx.files.external(MainSpaceCabal.QUEST_SAVE_LOCATION);
		for (FileHandle f : folder.list()) {
			Gdx.app.log(TAG, "packing " + f.nameWithoutExtension());

			QuestArray arr = json.fromJson(QuestArray.class, f);
			all.addAll(arr);
		}

		FileHandle outFile = Gdx.files.absolute(
				Gdx.files.internal(MainSpaceCabal.QUEST_SAVE_LOCATION + "allquests.json").file().getAbsolutePath()
				);
		String string = json.prettyPrint(all);
		outFile.writeString(string, false);
		Gdx.app.log(TAG, "packing all");

		Pools.free(json);
	}


	public final int seed;
	public int currentLevel;
	public int currentSystem;
	public int currentPlanet = 7;
	public int currentOrbitalDepth = 1;
	public static final int ORBIT_LANDED = 0, ORBIT_ORBIT = 1, ORBIT_ELLIPTICAL = 2;
	private static final String TAG = "Game Info";
	public PlanetNode.NodeType currentOrbit;
	public int xp;
	public transient SolarSystem[] systems = new SolarSystem[2];
	private IntMap<Quest> quests;

	
	public void createGalaxy(IntMap<Quest> questList){
		Gdx.app.log(TAG, "create galaxy" + questList.size);

		
		MathUtils.random.setSeed(seed);
		for (int i = 0; i < systems.length; i++){
			systems[i] = new SolarSystem(i, MathUtils.random(Integer.MAX_VALUE-1), this, questList);
		}
		
	}


	public boolean hasCompleted(int questHash, int systemIndex, int planetIndex) {
		if (true) return false;
		return systems[systemIndex].planets[planetIndex].completed.contains(questHash);
	}




	public Quest getQuest(int questHash) {
		
		return quests.get(questHash, null);
	}


	public Quest getQuest(String string) {
		if (string == null) return null;
		return getQuest(string.hashCode());
	}
	
	public IntMap<Quest> getQuests() {
		return quests;
	}

	Array<String> spawnedQuests = new Array<String>();
	public boolean hasSpawnedQuest(Quest quest) {
		
		return spawnedQuests.contains(quest.name, false);
	}


	public void onSpawnQuest(Quest quest) {
		if (!spawnedQuests.contains(quest.name, false))
			spawnedQuests.add(quest.name);
	}

	public Planet getCurrentPlanet() {
		return systems[currentSystem].planets[currentPlanet];
	}
	
	/*{
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
		basicQuests.add(nextQ);
	}*/
}
