package ninja.trek.actions;

import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.scenes.scene2d.ui.UI.WeaponButton;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.entity.Weapon;
import ninja.trek.WeaponItem;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AWeaponCharge extends Action {
	private static final String TAG = "weap charge action";
	
	
	public AWeaponCharge() {
		lanes = WEAPON_LANE_CHARGE;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		//Gdx.app.log(TAG, "update " + parent.e);
		//AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		//int tx = MathUtils.random(map.mapWidth-1);
		//int ty = MathUtils.random(map.mapHeight-1);
		Weapon w = (Weapon) parent.e;
		if (w.equippedItemIndex == -1) return;
		WeaponItem weI = (WeaponItem) Items.getDef(parent.e.ship.inventory.get(w.equippedItemIndex));
		
		int damage = weI.cost - w.totalCharge;
		damage = Math.min(damage, weI.chargeDrawLimit);
		if (damage <= 0) {
			WeaponButton butt = ui.weaponButtons[w.equippedItemIndex];
			butt.slider.setValue(1f);
			return;
		}
		//Gdx.app.log(TAG, "damage " + damage + " / " + w.totalCharge); 
		int target = Ship.WEAPON;
		//int replacement = Ship.WEAPON;
		int pending = parent.e.ship.depleter.getPath(parent.e.x, parent.e.y, target, damage);
		w.totalCharge += (damage - pending);
		float value = w.totalCharge / (float)(weI.cost - 1);
		//Gdx.app.log(TAG, "charged to " + w.totalCharge + " / " + weI.cost + "  dam " + damage + " pend" + pending);
		//parent.e.map.fill.clear();
		//parent.e.map.fill.floodFillDeplete(parent.e.map.map, parent.e.x, parent.e.y, target, replacement, damage);
		//wait.to.set(tx, ty);
		//addBeforeMe(wait);
		//AFollowPath follow = Pools.obtain(AFollowPath.class);
		//wait.addAfterMe(follow);
		//Gdx.app.log(TAG, "update " + parent.e.x + ", " + parent.e.y);
		WeaponButton butt = ui.weaponButtons[w.index];
	
		//value = 1.3f;
		butt.slider.setValue(value);
		//Gdx.app.log(TAG, "slider " + value);
	}

	
	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
		
	}

}
