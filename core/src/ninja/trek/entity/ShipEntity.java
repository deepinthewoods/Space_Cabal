package ninja.trek.entity;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABaseShip;
import ninja.trek.actions.AEnemyShip;
import ninja.trek.entity.Entity;

public class ShipEntity extends Entity implements Pool.Poolable{
	public int health ;
	public int totalHealth;
	public int shield ;
	public int shieldTotal;
	public int credits;
	public int fuel;
	public void reset(){
		health = 19000;
		totalHealth = 19000;
		shield = 4;
		shieldTotal = 0;
		credits = 100;
		fuel = 1000;
	}

	public ShipEntity() {
		glyph = " ";
		reset();
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
