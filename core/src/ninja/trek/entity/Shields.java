package ninja.trek.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.ActionList;
import ninja.trek.ui.ItemDisplay.ItemButton;

public class Shields extends SystemControlEntity {
	private static final String TAG = "shields";
	public int index;
	//public GridPoint2 target = new GridPoint2();

	public boolean hasTarget = false;
	public int equippedItemIndex = -1;
	public int equippedItemID;
	public int totalCharge;
	public int fireDelay;
	public transient Ship targetShip;
	public Shields(){
		setIcon("shield");
		otherButtons = new ButtonType[]{ButtonType.SHIELD_0, ButtonType.SHIELD_1, ButtonType.SHIELD_2, ButtonType.SHIELD_3, ButtonType.SHIELD_4,};
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

	@Override
	public void handleOtherButton(ButtonType type, World world) {
		ShipEntity shipE = ship.getShipEntity();
		switch (type){
			case SHIELD_0:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 0;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_1:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 1;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_2:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 2;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_3:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 3;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_4:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 4;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_5:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 5;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
			case SHIELD_6:
				Gdx.app.log(TAG, "" + type);
				shipE.shieldTotal = 6;
				shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				break;
		}
	}
}
