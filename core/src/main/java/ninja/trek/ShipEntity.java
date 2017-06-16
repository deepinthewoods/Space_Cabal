package ninja.trek;

import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABaseShip;
import ninja.trek.actions.ABaseWeapon;

public class ShipEntity extends Entity {
	public ShipEntity() {
		glyph = " ";
	}
	@Override
	public Entity setDefaultAI() {
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABaseShip.class));
		setAI(playerAction);
		return this;
	}
}
