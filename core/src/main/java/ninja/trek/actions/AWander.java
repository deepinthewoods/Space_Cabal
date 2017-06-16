package ninja.trek.actions;

import com.badlogic.gdx.Gdx;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AWander extends Action {
	private static final String TAG = "wander action";

	public AWander() {
		lanes = LANE_DELAY | LANE_ACTING;
	}
	@Override
	public void update(float dt, World world, Ship map) {
		Gdx.app.log(TAG, "update " + parent.e);
		
	}

	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
	}

}
