package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.AWeaponCharge;
import ninja.trek.actions.AWeaponShoot;

/**
 * Created by n on 22/11/2017.
 */

public class Door extends Entity implements Pool.Poolable {
    private static final String TAG = "Door e "
            ;
    public int radius;

    public Door(){

        glyph = "#";
        otherButtons = new ButtonType[]{ButtonType.DOOR_OPEN, ButtonType.DOOR_CLOSE};
        buttonOrder = null;
    }
    @Override
    public void reset() {
        super.reset();

    }

    public void clear() {

    }

    @Override
    public void handleOtherButton(ButtonType type) {
        switch (type){
            case DOOR_OPEN:
                Gdx.app.log(TAG, "OPEN");
                ship.openDoor(this);
                break;
            case DOOR_CLOSE:
                Gdx.app.log(TAG, "CLOSE");
                ship.closeDoor(this);
                break;
        }
    }

    @Override
    public Entity setDefaultAI() {
        resetAI();
        ActionList playerAction = new ActionList();
        playerAction.addToStart(Pools.obtain(ADoor.class));
        setAI(playerAction);
        Gdx.app.log(TAG, "DEFAULT AI");
        return this;
    }
}