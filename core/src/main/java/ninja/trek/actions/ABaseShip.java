package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABaseShip extends Action{
	private static final String TAG = "base ship action";
	
	public ABaseShip() {
		lanes = LANE_ACTING;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map) {
		int power = 10;
		//Gdx.app.log(TAG, "update " + parent.e);
		AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		Ship ship = parent.e.map;
		if (!ship.hasCategorizedBlocks) return;
		for (int i = 0; i < ship.systemButtonOrder.length; i++){
			if (power == 0) continue;
			int system = ship.systemButtonOrder[i];
			Array<GridPoint2> blocks = ship.systemBlocks[system];
			for (int r = 0; r < blocks.size; r++){
				GridPoint2 pt = blocks.get(r);
				int block = ship.map.get(pt.x, pt.y);
				int depletion = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
				int toRestore = Math.min(depletion, power);
				power -= toRestore;
				//if (toRestore != 0) Gdx.app.log(TAG, "restore " + toRestore);
				//else if (system == Ship.OXYGEN) Gdx.app.log(TAG, "oxy restore " + pt);
				depletion -= toRestore;
				block = block & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK);
				block |= depletion << Ship.BLOCK_DATA_BITS;
				ship.map.set(pt.x, pt.y, block);
			}
		}
	}

	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
	}

}
