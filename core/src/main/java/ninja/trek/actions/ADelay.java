package ninja.trek.actions;

import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ADelay extends Action {
	public ADelay() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		int action = parent.e.buttonOrder[parent.e.actionIndexForPath];
		
		int speedPC = parent.e.speed[action];
		parent.e.delayAccumulator += speedPC;
		if (parent.e.delayAccumulator >= 100){
			parent.e.delayAccumulator -= 100;
			isFinished = true;
		}

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
