package ninja.trek;

import com.badlogic.gdx.utils.Array;

public class QuestOption {
public String text = "option";
public String[] next = {"reward"};
public enum QuestCommand {NORMAL_REWARD, SMALL_REWARD, BIG_REWARD};
public enum QuestRequirement {HAS_ENGINES};
public Array<QuestCommand> postCommands = new Array<QuestOption.QuestCommand>();
public Array<QuestRequirement> requireAll = new Array<QuestRequirement>();
public QuestOption(String string) {
	text = string;
}
}
