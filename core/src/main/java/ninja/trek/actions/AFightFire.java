package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AFightFire extends Action {

	public GridPoint2 target = new GridPoint2();;
	
	public AFightFire(){
		lanes = LANE_ACTING;
		isBlocking = true;
	}

	@Override
	public void update(float dt, World world, Ship map) {
		map.map.fightFire(target.x, target.y);
		//Gdx.app.log("fix action", "FIX " + target);
		isFinished = true;
	}

	@Override
	public void onEnd(World world, Ship map) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart(World world, Ship map) {
		// TODO Auto-generated method stub

	}

}
