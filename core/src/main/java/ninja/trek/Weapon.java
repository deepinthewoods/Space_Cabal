package ninja.trek;

import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABase;
import ninja.trek.actions.ABaseWeapon;

public class Weapon extends Entity {
	public int index;
	
	public void clear() {
	}

	public void setIndex(int i) {
		index = i;
		glyph = ""+i;
	}
	
	@Override
	public Entity setDefaultAI() {
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABaseWeapon.class));
		setAI(playerAction);
		return this;
	}
	 
}
