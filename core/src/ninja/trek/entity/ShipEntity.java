package ninja.trek.entity;

import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABaseShip;
import ninja.trek.actions.AEnemyShip;
import ninja.trek.entity.Entity;

public class ShipEntity extends Entity {
	public int health = 9000;
	public int totalHealth = 19000;
	public int shield = 4;
	public int shieldTotal;
	public ShipEntity() {
		glyph = " ";
	}
	@Override
	public Entity setDefaultAI() {
		resetAI();
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(AEnemyShip.class));
		setAI(playerAction);
		return this;
	}
}
