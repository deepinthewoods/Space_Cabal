package ninja.trek.actions;

import com.badlogic.gdx.Gdx;

import ninja.trek.Laser;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ALaser extends Action{


	@Override
	public void update(float dt, World world, Ship map) {
		Laser las = (Laser) parent.e;
		las.time++;
		if (las.time > 4) isFinished = true;
		//Gdx.app.log("laser action", "update " + las.time);
	}

	@Override
	public void onEnd(World world, Ship map) {
		parent.e.map.removeLaser(((Laser)parent.e).index, parent.e);
	}

	@Override
	public void onStart(World world, Ship map) {
		// TODO Auto-generated method stub
		
	}

	

}
