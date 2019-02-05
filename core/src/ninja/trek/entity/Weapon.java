package ninja.trek.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship;
import ninja.trek.action.ActionList;
import ninja.trek.actions.AWeaponCharge;
import ninja.trek.actions.AWeaponShoot;
import ninja.trek.ui.ItemDisplay.ItemButton;

public class Weapon extends SystemControlEntity {
	public int index;
	//public GridPoint2 target = new GridPoint2();

	public boolean hasTarget = false;
	public int equippedItemIndex = -1;
	public int equippedItemID;
	public int totalCharge;
	public int fireDelay;
	public transient Ship targetShip;

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
@Override
public void reset() {
	equippedItemID = -1;
	equippedItemIndex = -1;
	super.reset();
}
	
	 
}
