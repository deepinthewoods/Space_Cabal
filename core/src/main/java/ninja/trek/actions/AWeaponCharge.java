package ninja.trek.actions;

import com.badlogic.gdx.Gdx;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.Weapon;
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
	public void update(float dt, World world, Ship map) {
		//Gdx.app.log(TAG, "update " + parent.e);
		//AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		//int tx = MathUtils.random(map.mapWidth-1);
		//int ty = MathUtils.random(map.mapHeight-1);
		Weapon w = (Weapon) parent.e;
		if (w.equippedItemIndex == -1) return;
		WeaponItem weI = (WeaponItem) Items.getDef(parent.e.ship.inventory.get(w.equippedItemIndex));

		
		
		int damage = weI.cost - w.totalCharge;
		if (damage <= 0) return;
		//Gdx.app.log(TAG, "damage " + damage + " / " + w.totalCharge); 
		int target = Ship.WEAPON;
		//int replacement = Ship.WEAPON;
		int pending = parent.e.ship.depleter.getPath(parent.e.x, parent.e.y, target, damage);
		w.totalCharge = weI.cost - pending;
		//Gdx.app.log(TAG, "charged to " + w.totalCharge + " / " + weI.cost + "  dam " + damage + " pend" + pending);
		//parent.e.map.fill.clear();
		//parent.e.map.fill.floodFillDeplete(parent.e.map.map, parent.e.x, parent.e.y, target, replacement, damage);
		//wait.to.set(tx, ty);
		//addBeforeMe(wait);
		//AFollowPath follow = Pools.obtain(AFollowPath.class);
		//wait.addAfterMe(follow);
		//Gdx.app.log(TAG, "update " + parent.e.x + ", " + parent.e.y);
	}

	
	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
		
	}

}
