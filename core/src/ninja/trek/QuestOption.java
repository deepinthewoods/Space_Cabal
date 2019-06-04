package ninja.trek;

import com.badlogic.gdx.utils.Array;

import ninja.trek.gen.GameInfo;
import ninja.trek.gen.Planet;

import static ninja.trek.QuestOption.QuestRequirement.PLANET_FRIENDLY;

public class QuestOption {

	public String getText(GameInfo info, Ship ship) {
		String costs = "";
		for (String command : commands){
			String[] split = command.split(" ");
			if (split[0].equals("cost")){

				int cost = Integer.parseInt(split[1]);
				if (split[2].equals("credits")){
					costs += " [" + split[1] + " credits]";
				}
				if (split[2].equals("fuel")){
					costs += " [" + split[1] + " fuel]";
				}
			}
		}

		return text + costs;
	}

	public enum QuestRequirement {HAS_ENGINES, HAS_ROCKET, HAS_TRANSPORTER, PLANET_FRIENDLY, PLANET_}
	public enum QuestSpawnRequirement {EARTH_LIKE, MARS_LIKE, MOON, SUN, GAS_GIANT, INNER_PLANET, METEOR};
	public static final int COMMAND_REWARD;;
	public String text = "option";
	public String[] next = {};
	//public Array<QuestCommand> postCommands = new Array<QuestOption.QuestCommand>();
	public String[] commands = {};// = {"nextquest"};
	public Array<QuestRequirement> requireAll = new Array<QuestRequirement>();
	public Array<QuestRequirement> requireAny = new Array<QuestRequirement>();
	private static final String REWARD = "reward";
	static {
		COMMAND_REWARD = QuestOption.hash(REWARD);
	}
	public QuestOption(String string) {
		text = string;
	}
	public QuestOption() {
		
	}
	public static int hash(String string) {
		int hash = 0;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			hash *= c;
		}
		return hash;
	}

	public boolean isValid(GameInfo info, Planet planet, Ship ship) {
		boolean valid = requireAll.size == 0;
		for (int i = 0; i < requireAll.size; i++){
			QuestRequirement r = requireAll.get(i);
			if (optionValid(info, planet, r, ship)) valid = true;

			if (!valid) return false;
		}
		valid = requireAny.size == 0;;
		for (int i = 0; i < requireAll.size; i++){
			QuestRequirement r = requireAll.get(i);
			if (optionValid(info, planet, r, ship)) valid = true;

		}
		if (!valid) return false;
		return true;
	}

	private boolean optionValid(GameInfo info, Planet planet, QuestRequirement r, Ship ship){

		for (String command : commands){
			String[] split = command.split(" ");
			if (split[0].equals("cost")){

				int cost = Integer.parseInt(split[1]);
				if (split[2].equals("credits")){
					if (ship.getShipEntity().credits < cost) return false;
				}
				if (split[2].equals("fuel")){
					if (ship.getShipEntity().fuel < cost) return false;
				}
			}
		}


		switch (r){
			case PLANET_FRIENDLY: if (planet.isFriendly) return true;
		}

		return false;
	}

}