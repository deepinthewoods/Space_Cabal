package ninja.trek;

import com.badlogic.gdx.utils.Array;

public class Quest {
	transient public String name;
	public boolean isOneOff = false
			;
	
	public Array<QuestOption> options = new Array<QuestOption>();
	public String text = "Explanation text";
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return name.hashCode();
	}
}
