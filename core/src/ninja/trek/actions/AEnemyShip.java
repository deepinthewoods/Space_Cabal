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
        if (!parent.e.ship.isHostile || world.getPlayerShip() == parent.e.ship){
            super.update(dt, world, map, ui);
            return;
        }
        ///Gdx.app.log(TAG, "UPDATE ENEMY SHIP" + parent.e.ship);

        Ship ship = parent.e.ship;
        ShipEntity shipE = ship.getShipEntity();
        Array<Entity> weapons = ship.getEntitiesByClass(Weapon.class);
        for (int i = 0; i < weapons.size; i++){
            Weapon weapon = (Weapon) weapons.get(i);
            Ship tShip = world.getPlayerShip();
            target.set(tShip.mapWidth/2, tShip.mapHeight/2);
           // ship.setWeaponTarget(weapon.index, missileTarget.x, missileTarget.y, targetShip);
            //Gdx.app.log(TAG, "set weapon targets");
        }
        //this.delay(2f);
        if (ship.getShipEntity().health < 0){
            world.shipDeath(ship);
            isFinished = true;

        }

        super.update(dt, world, map, ui);

    }

    @Override
    public void onEnd(World world, Ship map) {

    }

    @Override
    public void onStart(World world, Ship map) {

    }
}
