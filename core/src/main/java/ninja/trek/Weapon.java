package ninja.trek;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.AWeaponCharge;
import ninja.trek.actions.AWeaponShoot;
import ninja.trek.ui.ItemDisplay.ItemButton;

public class Weapon extends Entity {
	public int index;
	public GridPoint2 target = new GridPoint2();
	public boolean hasTarget = false;
	public int equippedItemIndex;
	public int equippedItemID;
	public int totalCharge;
	public int fireDelay;
	
	public void clear() {
	}

	public void setIndex(int i) {
		index = i;
		glyph = ""+i;
	}
	
	@Override
	public Entity setDefaultAI() {
		resetAI();
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(AWeaponShoot.class));
		playerAction.addToStart(Pools.obtain(AWeaponCharge.class));
		setAI(playerAction);
		return this;
	}

	public void equip(ItemButton item) {
		equippedItemIndex = item.index;
		equippedItemID = item.itemID;
	}

	public void unequip() {
		equippedItemIndex = -1;//item.index;
		equippedItemID = -1;//item.itemID;
		
	}

	
	 
}
