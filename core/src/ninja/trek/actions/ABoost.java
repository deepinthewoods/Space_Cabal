package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABoost extends Action {


	private static final String TAG = "boost";

	public ABoost() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}

	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		GridPoint2 target = parent.e.target;
		int block = map.map.get(target.x, target.y);
		int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
		int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
		if (currentBoost == 0 && currentFire == 0 && currentDam == 0){
			map.map.boost(target.x, target.y);
			//Gdx.app.log("boost action", "boost " + missileTarget + "  " + block + " " + map.map.get(missileTarget.x, missileTarget.y));
			if (parent.e.x != target.x || parent.e.y != target.y) Gdx.app.log("aboost", "WRONG PLACE" + target);
		} else {
			if (parent.e.x != target.x || parent.e.y != target.y) Gdx.app.log("aboost", "WRONG PLACE" + target);
			//Gdx.app.log("boost action", "fail not valid " + missileTarget + "  " + (block& Ship.BLOCK_BOOST_MASK)+ " " + map.map.get(missileTarget.x, missileTarget.y));
			map.map.needsBoost[block & Ship.BLOCK_ID_MASK].remove(parent.e.x + parent.e.y * map.mapWidth, 0);
			
			//map.map.boost(missileTarget.x, missileTarget.y);
		}
		if (parent.e.target.x != parent.e.x || parent.e.target.y != parent.e.y) Gdx.app.log("boost action", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		isFinished = true;
	}

	@Override
	public void onEnd(World world, Ship map) {
		parent.e.ship.unReserve(parent.e.target.x, parent.e.target.y);
		//Gdx.app.log(TAG, "end");
	}

	@Override
	public void onStart(World world, Ship map) {
		//Gdx.app.log(TAG, "start");
	}

}
