package ninja.trek.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.AEngineCharge;
import ninja.trek.actions.AWeaponCharge;
import ninja.trek.actions.AWeaponShoot;
import ninja.trek.ui.ItemDisplay.ItemButton;

public class Engine extends SystemControlEntity {
	public static final int DODGE_COST = 100;
	public int index;
	public GridPoint2 target = new GridPoint2();
	public boolean hasTarget = false;
	public int totalCharge;


	public Engine(){
		glyph = "En";
		setIcon("gears");
		//otherButtons = new ButtonType[]{ButtonType.DOOR_OPEN, ButtonType.DOOR_CLOSE};
		//otherButtons = new ButtonType[]{ButtonType.DOOR_OPEN, ButtonType.DOOR_CLOSE};
		buttonOrder = null;
	}



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
		playerAction.addToStart(Pools.obtain(AEngineCharge.class));
		setAI(playerAction);
		return this;
	}


@Override
public void reset() {

	super.reset();
}
	
	 
}
