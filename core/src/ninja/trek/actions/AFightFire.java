package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AFightFire extends Action {

	
	public AFightFire(){
		lanes = LANE_ACTING;
		isBlocking = true;
	}

	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		map.map.fightFire(parent.e.target.x, parent.e.target.y);
		Gdx.app.log("fix action", "FIRE " + parent.e.target);
		isFinished = true;
	}

	@Override
	public void onEnd(World world, Ship map) {
		parent.e.ship.unReserve(parent.e.target.x, parent.e.target.y);


	}

	@Override
	public void onStart(World world, Ship map) {
		// TODO Auto-generated method stub

	}

}
