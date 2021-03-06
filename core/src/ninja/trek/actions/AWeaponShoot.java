package ninja.trek.actions;

import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.entity.Weapon;
import ninja.trek.WeaponItem;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AWeaponShoot extends Action {
	public AWeaponShoot() {
		lanes = Action.WEAPON_LANE_SHOOTING;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		Weapon w = (Weapon) parent.e;
		if (w.equippedItemIndex == -1) return;

		WeaponItem weI = (WeaponItem) Items.getDef(parent.e.ship.inventory.get(w.equippedItemIndex));
		if (w.targetShip == null || w.targetShip.getShipEntity().health < 0) return;
		w.fireDelay--;
		if (w.totalCharge >= weI.cost && w.fireDelay <= 0){
			if (!w.hasTarget) return;
			w.totalCharge -= weI.cost;
			//Gdx.app.log("aweaponshoot", "shoot");
			shoot(w, weI);
		}

	}
	private void shoot(Weapon w, WeaponItem weI) {
		
		parent.e.ship.shoot(weI, w.target, w.targetShip, w);
		w.fireDelay = (int)(weI.fireWindDownTime / World.timeStep);
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
