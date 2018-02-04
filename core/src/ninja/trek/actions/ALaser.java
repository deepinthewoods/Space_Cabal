package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Items;
import ninja.trek.Laser;
import ninja.trek.Ship;
import ninja.trek.WeaponItem;
import ninja.trek.World;
import ninja.trek.action.Action;
import ninja.trek.items.LaserItem;
import ninja.trek.items.MissileItem;

public class ALaser extends Action{


    private World world;
    private Ship map;

    @Override
	public void update(float dt, World world, Ship map, UI ui) {
		Laser las = (Laser) parent.e;
        //WeaponItem weI = (WeaponItem) Items.getDef(weeaponItemID);
		las.time++;
		if (las.time > 1) isFinished = true;

	}

	@Override
	public void onEnd(World world, Ship map) {
		parent.e.ship.removeLaser(((Laser)parent.e).index, parent.e);
	}

	@Override
	public void onStart(World world, Ship map) {
        this.world = world;
        this.map = map;
        shoot();
    }

    public void shoot() {
        Laser las = (Laser) parent.e;
        LaserItem weI = (LaserItem) Items.getDef(las.weaponItemID);
        Ship target = world.getEnemy(map);

        if (target.getShipEntity().shield <= weI.shieldPiercing)
            target.laserDamage(las.target.x, las.target.y, weI.damage);
    }


}
