package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABase extends Action {
	private static final String TAG = "base action";
	
	public ABase() {
		lanes = LANE_ACTING;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		//Gdx.app.log(TAG, "update " + parent.e);
		AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		int tx = MathUtils.random(map.mapWidth-1);
		int ty = MathUtils.random(map.mapHeight-1);
		
		wait.to.set(tx, ty);
		addBeforeMe(wait);
		
		
	}

	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
	}

}
