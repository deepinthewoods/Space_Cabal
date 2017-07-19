package ninja.trek;

import com.badlogic.gdx.utils.Array;

public class Quest {
	public String name;
	public String[] commands;
	public String text = "Explanation text";
	public String[] tags = {};
	public boolean isOneOff = false;
	public Array<QuestOption> options = new Array<QuestOption>();
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
