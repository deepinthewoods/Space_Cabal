package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.EntityAI;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AFollowPath extends Action {
	private static final String TAG = "follow path a";
	transient private int pathProgress;
	public AFollowPath() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		//Gdx.app.log(TAG, "update " + parent.e.path.size + " " + pathProgress);
		int pathx = 0;//parent.e.path.get(pathProgress<<1);
		int pathy = 0;//parent.e.path.get((pathProgress<<1)+1);
		if (parent.e.path.size == 0){
			//isFinished = true;
			//return;
			pathx = parent.e.x;
			pathy = parent.e.y;
		} else {
			pathx = parent.e.path.get(pathProgress<<1);
			pathy = parent.e.path.get((pathProgress<<1)+1);
		}
		int dx = pathx - parent.e.x;
		int dy = pathy - parent.e.y;
		dx = Math.max(-1, Math.min(1, dx));
		dy = Math.max(-1, Math.min(1, dy));
		parent.e.x += dx;
		parent.e.y += dy;
		
		if (Math.abs(dx) > 1 && Math.abs(dy) > 1){
			Gdx.app.log(TAG, "OVERLONG" + dx + "  " + dy);
		}
		//Gdx.app.log(TAG, "move " + parent.x + " - " + x + "  ,  " + parent.y + " - " + y + "  diff " + dx + ", " + dy);
		if (parent.e.x == pathx && parent.e.y == pathy) {
			pathProgress--;
			//Gdx.app.log(TAG, "progress" + pathx + ", " + pathy);
		}
		ADelay delay = Pools.obtain(ADelay.class);
		addBeforeMe(delay);
		if (pathProgress < 0 ){
			switch (parent.e.actionIndexForPath){
			case EntityAI.FIX:
				AFix aFix = Pools.obtain(AFix.class);
				if (parent.e.path.size == 0)
					aFix.target.set(parent.e.x, parent.e.y);
				else
					aFix.target.set(parent.e.path.get(0), parent.e.path.get(1));
				addBeforeMe(aFix);
				break;
			case EntityAI.FIRE:
				AFightFire aFire = Pools.obtain(AFightFire.class);
				if (parent.e.path.size == 0)
					aFire.target.set(parent.e.x, parent.e.y);					
				else 
					aFire.target.set(parent.e.path.get(0), parent.e.path.get(1));
				addBeforeMe(aFire);
				break;
			case EntityAI.ENGINE:
			case EntityAI.SHIELDS:
			case EntityAI.OXYGEN:
			case EntityAI.DRONE:
			case EntityAI.WEAPON:
			case EntityAI.TELEPORTER:
			case EntityAI.SCIENCE:
				ABoost aBoost = Pools.obtain(ABoost.class);
				if (parent.e.path.size == 0){
					parent.e.target.set(parent.e.x, parent.e.y);
					//parent.e.ship.unReserve(parent.e.x, parent.e.y);
				}
				else{ 
					//Gdx.app.log(TAG, "non0 path " + parent.e.path); 
					parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
				}
				
				addBeforeMe(aBoost);
				//Gdx.app.log(TAG, "boost unreserve " + parent.e.x + "," + parent.e.y);
				
				break;
			case EntityAI.SHOOT:
				break;
			case EntityAI.WANDER:
				break;
			}
			//Pools.free(parent.e.path);
			parent.e.path = null;
			isFinished = true;
			//Gdx.app.log(TAG, "stop move" + parent.e.x + ", " + parent.e.y + " for " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]]);

				//aBoost.target.set(parent.e.path.get(0), parent.e.path.get(1));
			
		} else {
			/*if (!isStillValid(parent.e.path.get(0), parent.e.path.get(1), parent.e.actionIndexForPath)){
				parent.e.ship.unReserve(parent.e.path.get(0), parent.e.path.get(1));
				Pools.free(parent.e.path);
				parent.e.path = null;
				isFinished = true;
			}*/
		}
		
	}
	/*
	 * int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
	 */

	private boolean isStillValid(int x, int y, int actionIndexForPath) {
		int block = parent.e.ship.map.get(x,  y);
		switch (parent.e.buttonOrder[actionIndexForPath]){
		case EntityAI.ENGINE:
		case EntityAI.OXYGEN:
		case EntityAI.DRONE:
		case EntityAI.SHIELDS:
		case EntityAI.WEAPON:
		case EntityAI.TELEPORTER:
		case EntityAI.SCIENCE:
			int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
			int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
			if (currentDep == 0 && currentBoost == 0) return true;
			return false;
		}
		return true;
	}
	@Override
	public void onEnd(World world, Ship map) {
		//Gdx.app.log(TAG, "follow finished " + parent.e.x + "," + parent.e.y + " target " + parent.e.target + "  prog " + pathProgress);
	}

	@Override
	public void onStart(World world, Ship map) {
		if (parent.e.path == null) throw new GdxRuntimeException("null path " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]]);

		pathProgress = parent.e.path.size/2-1;
		//Gdx.app.log(TAG, "follow start " );
	}

}
