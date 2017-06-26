package ninja.trek;

import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABaseShip;
import ninja.trek.actions.AWeaponCharge;

public class ShipEntity extends Entity {
	public ShipEntity() {
		glyph = " ";
	}
	@Override
	public Entity setDefaultAI() {
		resetAI();
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABaseShip.class));
		setAI(playerAction);
		return this;
	}
}
