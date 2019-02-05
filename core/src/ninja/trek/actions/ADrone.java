package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;
import ninja.trek.Drone;

public class ADrone extends Action {
    private static final String TAG = "drone action ";

    @Override
    public void update(float dt, World world, Ship map, UI ui) {
        Drone drone = (Drone) parent.e.ship;
        drone.offsetWorld.rotate(0.25f  );
        //Gdx.app.log(TAG, "update drone" + drone.offsetWorld);
    }

    @Override
    public void onEnd(World world, Ship map) {



    }

    @Override
    public void onStart(World world, Ship map) {
        Drone drone = (Drone) parent.e.ship;
        drone.offsetWorld.set(drone.parent.mapWidth/2, 0);
        drone.offsetWorld.set(0,0);
    }
}
