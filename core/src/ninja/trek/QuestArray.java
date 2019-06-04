package ninja.trek;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

public class QuestArray extends Array<Quest> implements Poolable{

	@Override
	public void reset() {
		clear();
	}

	public String[] toStringArray() {
		String[] s = new String[size];
		for (int i = 0; i < s.length; i++){
			s[i] = get(i).name;
		}
		return s;
	}
}
