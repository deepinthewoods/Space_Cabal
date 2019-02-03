package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;

import ninja.trek.Ship;
import ninja.trek.entity.Engine;
import ninja.trek.entity.Entity;
import ninja.trek.entity.ShipEntity;
import ninja.trek.World;
import ninja.trek.entity.Weapon;

public class AEnemyShip extends ABaseShip {

    private static final String TAG = "enemy ship action ";
    public static GridPoint2 target = new GridPoint2();
    @Override
    public void update(float dt, World world, Ship map, UI ui) {

        Ship ship = parent.e.ship;
        ShipEntity shipE = ship.getShipEntity();
        Array<Entity> weapons = ship.getEntitiesByClass(Engine.class);
        for (int i = 0; i < weapons.size; i++){
            Weapon weapon = (Weapon) weapons.get(i);
            Ship tShip = world.getPlayerShip();
            target.set(tShip.mapWidth/2, tShip.mapHeight/2);
            ship.setWeaponTarget(weapon.index, target.x, target.y);
            Gdx.app.log(TAG, "set weapon targets");
        }
        this.delay(2f);

        super.update(dt, world, map, ui);

    }

    @Override
    public void onEnd(World world, Ship map) {

    }

    @Override
    public void onStart(World world, Ship map) {

    }
}
