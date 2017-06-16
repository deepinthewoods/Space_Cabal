package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABaseWeapon extends Action {
	private static final String TAG = "base action";
	
	public ABaseWeapon() {
		lanes = LANE_ACTING;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map) {
		//Gdx.app.log(TAG, "update " + parent.e);
		AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		int tx = MathUtils.random(map.mapWidth-1);
		int ty = MathUtils.random(map.mapHeight-1);
		
		int damage = 10;
		int target = Ship.WEAPON;
		int replacement = Ship.WEAPON;
		parent.e.map.depleter.getPath(parent.e.x, parent.e.y, target, damage);
		//parent.e.map.fill.clear();
		//parent.e.map.fill.floodFillDeplete(parent.e.map.map, parent.e.x, parent.e.y, target, replacement, damage);
		wait.to.set(tx, ty);
		//addBeforeMe(wait);
		AFollowPath follow = Pools.obtain(AFollowPath.class);
		//wait.addAfterMe(follow);
		//Gdx.app.log(TAG, "update " + parent.e.x + ", " + parent.e.y);
	}

	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
	}

}
