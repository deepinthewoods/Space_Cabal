package ninja.trek;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

public class QuestArray extends Array<Quest> implements Poolable{

	@Override
	public void reset() {
		clear();
	}

}
