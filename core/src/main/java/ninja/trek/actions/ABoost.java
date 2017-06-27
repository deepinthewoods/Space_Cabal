package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABoost extends Action {
	
	public GridPoint2 target = new GridPoint2();;
	
	public ABoost() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}

	@Override
	public void update(float dt, World world, Ship map) {
		int block = map.map.get(target.x, target.y);
		int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
		int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
		if (currentBoost == 0 && currentFire == 0 && currentDam == 0){
			map.map.boost(target.x, target.y);
			//Gdx.app.log("boost action", "boost " + target + "  " + block + " " + map.map.get(target.x, target.y));
			if (parent.e.x != target.x || parent.e.y != target.y) Gdx.app.log("aboost", "WRONG PLACE" + target);
		} else {
			if (parent.e.x != target.x || parent.e.y != target.y) Gdx.app.log("aboost", "WRONG PLACE" + target);
			//Gdx.app.log("boost action", "fail not valid " + target + "  " + (block& Ship.BLOCK_BOOST_MASK)+ " " + map.map.get(target.x, target.y));
			map.map.boost(target.x, target.y);
		}
		if (parent.e.target.x != parent.e.x || parent.e.target.y != parent.e.y) Gdx.app.log("boost action", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		isFinished = true;
	}

	@Override
	public void onEnd(World world, Ship map) {
		// TODO Auto-generated method stub
		parent.e.ship.unReserve(parent.e.target.x, parent.e.target.y);
	}

	@Override
	public void onStart(World world, Ship map) {
		// TODO Auto-generated method stub

	}

}
