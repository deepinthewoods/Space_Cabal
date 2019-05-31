package ninja.trek;

import com.badlogic.gdx.utils.Array;

public class QuestOption {
	public enum QuestCommand {NORMAL_REWARD, SMALL_REWARD, BIG_REWARD};
	public enum QuestRequirement {HAS_ENGINES, HAS_ROCKET, HAS_TRANSPORTER}
	public enum QuestSpawnRequirement {EARTH_LIKE, MARS_LIKE, MOON, SUN, GAS_GIANT, INNER_PLANET, METEOR};
	public static final int COMMAND_REWARD;;
	public String text = "option";
	public String[] next = {};
	//public Array<QuestCommand> postCommands = new Array<QuestOption.QuestCommand>();
	public String[] commands;// = {"nextquest"};
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
}