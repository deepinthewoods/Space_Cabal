package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ABase;

public class Entity implements Poolable {
	private static final String TAG = "entity";
	public int x, y;
	public String glyph = "A";
	public int font = 1;
	private transient EntityAI ai;
	public transient Ship map;
	public int[] buttonOrder = new int[EntityAI.names.length];
	public int[] fixOrder = new int[FIX_ACTIONS_LENGTH];//block ids
	public static final int FIX_ACTIONS_LENGTH = 5;

	public Entity(){
		reset();
	}
	
	public Entity pos(int x, int y){
		this.x = x;
		this.y = y;
		return this;
	}
	public void update(World world){
		ai.update(world, map);
	};
	
	public Entity setAI(EntityAI ai){
		this.ai = ai;
		ai.setParent(this);
		return this;
	}
	public Entity setMap(Ship map){
		
		this.map = map;			
		return this;
	}

	@Override
	public void reset() {
		for (int i = 0; i < buttonOrder.length; i++){
			buttonOrder[i] = i;
		}
		
	}
	int[] ret = new int[2];
	public IntArray path;
	public int actionIndexForPath;
	
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
				case EntityAI.POWER:fixOrder[prog++] = Ship.POWER;break;
				case EntityAI.WEAPON:fixOrder[prog++] = Ship.WEAPON;break;
			}
		}
		
	}

	public Entity setDefaultAI() {
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABase.class));
		setAI(playerAction);
		return this;
	}

	public Entity pos(GridPoint2 pos) {
		x = pos.x;
		y = pos.y;
		return this;
	}
	
}
