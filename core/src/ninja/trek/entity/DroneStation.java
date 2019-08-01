package ninja.trek.entity;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.ActionList;
import ninja.trek.ui.ItemDisplay.ItemButton;

public class DroneStation extends SystemControlEntity {
	public int index;
	//public GridPoint2 target = new GridPoint2();

	public boolean hasTarget = false;
	public int equippedItemIndex = -1;
	public int equippedItemID;
	public int totalCharge;
	public int fireDelay;
	public transient Ship targetShip;
	public DroneStation(){
		setIcon("delivery-drone");
		otherButtons = new ButtonType[]{ButtonType.LAUNCH_DRONE_0, ButtonType.STOP_DRONE_0, ButtonType.LAUNCH_DRONE_1, ButtonType.STOP_DRONE_1
				, ButtonType.LAUNCH_DRONE_2, ButtonType.STOP_DRONE_2, ButtonType.LAUNCH_DRONE_3, ButtonType.STOP_DRONE_3};
		buttonOrder = null;
	}

	@Override
	public void handleOtherButton(ButtonType type, World world) {
		ShipEntity shipE = ship.getShipEntity();
		switch (type) {
			case LAUNCH_DRONE_0: world.addDrone("dronebasic", ship); break;
			case STOP_DRONE_0: break;
			case LAUNCH_DRONE_1: world.addDrone("dronebasic", ship); break;
			case STOP_DRONE_1: break;
		}
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
		//playerAction.addToStart(Pools.obtain(AWeaponShoot.class));
		//playerAction.addToStart(Pools.obtain(AWeaponCharge.class));
		setAI(playerAction);
		return this;
	}

	public void equip(ItemButton item) {
		equippedItemIndex = item.index;
		equippedItemID = item.itemID;
		setIcon(Items.getDef(item.itemID).icon);
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

	@Override
	public void drawBackground(SpriteBatch batch, OrthographicCamera camera, World world, Texture backgroundTexture) {

	}
}
