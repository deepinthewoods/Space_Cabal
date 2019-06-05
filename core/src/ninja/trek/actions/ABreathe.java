package ninja.trek.actions;

import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABreathe extends Action {
    @Override
    public void update(float dt, World world, Ship map, UI ui) {
        map.breathe(parent.e);
    }

    @Override
    public void onEnd(World world, Ship map) {

    }

    @Override
    public void onStart(World world, Ship map) {

    }
}
