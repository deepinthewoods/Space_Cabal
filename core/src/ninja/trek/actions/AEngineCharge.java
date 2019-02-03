package ninja.trek.actions;

import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.scenes.scene2d.ui.UI.WeaponButton;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.WeaponItem;
import ninja.trek.World;
import ninja.trek.action.Action;
import ninja.trek.entity.Engine;
import ninja.trek.entity.Weapon;

public class AEngineCharge extends Action {
	private static final String TAG = "weap charge action";


	public AEngineCharge() {
		lanes = WEAPON_LANE_CHARGE;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		//Gdx.app.log(TAG, "update " + parent.e);
		//AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		//int tx = MathUtils.random(map.mapWidth-1);
		//int ty = MathUtils.random(map.mapHeight-1);
		Engine w = (Engine) parent.e;
		//if (w.equippedItemIndex == -1) return;
		//WeaponItem weI = (WeaponItem) Items.getDef(parent.e.ship.inventory.get(w.equippedItemIndex));
		
		int damage = Engine.DODGE_COST - w.totalCharge;
		//damage = Math.min(damage, weI.chargeDrawLimit);

		//Gdx.app.log(TAG, "damage " + damage + " / " + w.totalCharge); 
		int target = Ship.ENGINE;
		//int replacement = Ship.WEAPON;
		int pending = parent.e.ship.depleter.getPath(parent.e.x, parent.e.y, target, damage);
		w.totalCharge += (damage - pending);

		//float value = w.totalCharge / (float)(Engine.DODGE_COST - 1);
		//WeaponButton butt = ui.weaponButtons[w.index];
		//butt.slider.setValue(value);
		//Gdx.app.log(TAG, "slider " + value);
	}

	
	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
		
	}

}
