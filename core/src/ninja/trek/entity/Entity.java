package ninja.trek.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

import ninja.trek.EntityAI;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.ActionList;
import ninja.trek.actions.ABase;

public class Entity implements Poolable {
	private static final String TAG = "entity";
	public static String[] raceNames = {"Human", "Reptiod", "Sloth", "Mechanoid", "System"};
	public static int RACE_HUMAN = 0, RACE_REPTOID = 1, RACE_SLOTH = 2, RACE_MECHANOID = 3, RACE_SYSTEM = 4;
	public int x, y;
	public String glyph = "A";
	public int font = 0;
	private transient EntityAI ai;
	public transient Ship ship;
	public int[] buttonOrder = new int[EntityAI.names.length];
	public boolean[] disabledButton = new boolean[EntityAI.names.length];
	public GlyphLayout glyphLayout = new GlyphLayout()
			;

	public void handleOtherButton(ButtonType type) {

	}

	public enum ButtonType {DOOR_OPEN, DOOR_CLOSE, SHIELD_UP, SHIELD_DOWN };
	public ButtonType[] otherButtons = null;
	public int[] fixOrder = new int[FIX_ACTIONS_LENGTH];//block ids
	public static final int FIX_ACTIONS_LENGTH = 7;
	public transient GridPoint2 target = new GridPoint2();
	public int delayAccumulator;
	public boolean isHostile = false;;
	public Entity(){
		reset();
	}
	
	public Entity pos(int x, int y){
		this.x = x;
		this.y = y;
		return this;
	}
	public void update(World world, UI ui){
		if (ai == null){
			Gdx.app.log(TAG, "no ai " + glyph);
			return;
		}
		ai.update(world, ship, ui);
	};
	
	public Entity setAI(EntityAI ai){
		resetAI();
		this.ai = ai;
		ai.setParent(this);
		return this;
	}
	protected void resetAI() {
		if (this.ai != null){
			//Gdx.app.log(TAG, "non null ai");
			ai.clear();
			Pools.free(ai);
			ai = null;
		}		
	}

	public Entity setMap(Ship map){
		this.ship = map;			
		return this;
	}

	@Override
	public void reset() {
		for (int i = 0; i < buttonOrder.length; i++){
			buttonOrder[i] = i;
		}
		delayAccumulator = 0;
		for (int i = 0; i < speed.length; i++){
			speed[i] = 90;
		}
	}
	
	int[] ret = new int[2];
	public IntArray path;
	public int actionIndexForPath;
	public int[] speed = new int[EntityAI.names.length];
	
	/**
	 * @param a value
	 * @param b value
	 */
	public int[] swap(int a, int b) {
		int aind = 0;
		for (int i = 0; i < buttonOrder.length; i++) if (buttonOrder[i] == a) aind = i;
		int bind = 0;
		for (int i = 0; i < buttonOrder.length; i++) if (buttonOrder[i] == b) bind = i;
		int c = buttonOrder[aind];
		//Gdx.app.log(TAG, "SWAP " + aind + "  " + bind);
		//Gdx.app.log(TAG, "SWVA " + buttonOrder[aind] + "  " + buttonOrder[bind]);
		buttonOrder[aind] = buttonOrder[bind];
		buttonOrder[bind] = c;
		ret[0] = aind;
		ret[1] = bind;
		updateFixOrder();
		return ret;
	}

	private void updateFixOrder() {
		int prog = 0;
		for (int i = 0; i < buttonOrder.length; i++){
			switch (buttonOrder[i]){
				case EntityAI.ENGINE:
					fixOrder[prog++] = Ship.ENGINE;
					break;
				case EntityAI.SHIELDS:
					fixOrder[prog++] = Ship.SHIELD;
					break;
				case EntityAI.OXYGEN:fixOrder[prog++] = Ship.OXYGEN;break;
				case EntityAI.DRONE:fixOrder[prog++] = Ship.DRONE;break;
				case EntityAI.WEAPON:fixOrder[prog++] = Ship.WEAPON;break;
				case EntityAI.TELEPORTER:fixOrder[prog++] = Ship.TELEPORTER;break;
				case EntityAI.SCIENCE:fixOrder[prog++] = Ship.SCIENCE;break;
			}
		}
	}

	public Entity setDefaultAI() {
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABase.class));
		setAI(playerAction);
		//setDefaultButtonOrder();
		return this;
	}

	public void setDefaultButtonOrder() {
		buttonOrder[0] = EntityAI.FIRE;
		buttonOrder[1] = EntityAI.FIX;
		buttonOrder[2] = EntityAI.SHOOT;
		buttonOrder[3] = EntityAI.WEAPON;
		buttonOrder[4] = EntityAI.SHIELDS;
		buttonOrder[5] = EntityAI.DRONE;
		buttonOrder[6] = EntityAI.ENGINE;
		buttonOrder[7] = EntityAI.SCIENCE;
		buttonOrder[8] = EntityAI.OXYGEN;
		buttonOrder[9] = EntityAI.TELEPORTER;
		buttonOrder[10] = EntityAI.WANDER;
		updateFixOrder();
		
	}

	public Entity pos(GridPoint2 pos) {
		x = pos.x;
		y = pos.y;
		return this;
	}

	public void draw(SpriteBatch batch, OrthographicCamera camera, World world) {
		// TODO Auto-generated method stub
		
	}
	
}
