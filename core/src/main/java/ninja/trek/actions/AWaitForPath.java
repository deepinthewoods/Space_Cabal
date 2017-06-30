package ninja.trek.actions;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.EntityAI;
import ninja.trek.IntPixelMap;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AWaitForPath extends Action {

	private static final String TAG = "path wait a"
			;
	public GridPoint2 to = new GridPoint2();
	private boolean hasStartedPath;
	private IntArray path, found = new IntArray();
	
	public AWaitForPath() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map) {
		if (!hasStartedPath){
			hasStartedPath = true;			
		}
		if (hasStartedPath){
			//path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, parent.e.buttonOrder, parent.e.fixOrder);
			//parent.e.path = path;
			
			//Gdx.app.log(TAG, "path for " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] + "  size" + path.size);
			//Gdx.app.log(TAG, "found path, size " + path.size + " to " + to + parent.e.actionIndexForPath);
			int actionIndexForPath = 0;
			IntPixelMap m = parent.e.ship.map;
			//Gdx.app.log(TAG, "update " + parent.e);
			for (int i = 0; i < parent.e.buttonOrder.length; i++){
				int action = parent.e.buttonOrder[i];
				//Gdx.app.log(TAG, "action " + EntityAI.names[action]);
				int blockID = getBlockID(action);
				
				switch (action){
				case EntityAI.ENGINE:
				case EntityAI.OXYGEN:
				case EntityAI.POWER:
				case EntityAI.WEAPON:
				case EntityAI.SHIELDS:
					IntIntMap list = m.needsBoost[blockID];
					//found.clear();
					//Gdx.app.log(TAG, "STARTRTARTSDGTDFSFD " + list.length());
					
					if (list.size != 0){
						//if (list.nextClearBit(0) == -1) return;
						int smallDist = 1000000;
						int distIndex = -1;
						Iterator<Entry> iter = list.iterator();
						while (iter.hasNext()){
							Entry ent = iter.next();
							int ind = ent.key;
							int x = ind % parent.e.ship.mapWidth;
							int y = ind / parent.e.ship.mapWidth;
							
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);
							if (dist < smallDist && ( block & Ship.BLOCK_DATA_MASK ) == 0 && !parent.e.ship.isReserved(x, y)){
								smallDist = dist;
								distIndex = ind;
							}
							//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  ");
							
						}
						
						if (distIndex == -1) break;
						
						int targetX = distIndex % parent.e.ship.mapWidth;
						int targetY = distIndex / parent.e.ship.mapWidth;
						path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, targetX, targetY);
						boolean boosted = (m.get(parent.e.x, parent.e.y) & Ship.BLOCK_BOOST_MASK) != 0;
						if (path.size == 0 && boosted ){
							Pools.free(path);
							path = null;
							//Gdx.app.log(TAG, "0 path ");// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);

							break;
						}
						if (path == null){
							Gdx.app.log(TAG, "NULL PATH");
							return;
						}
						parent.e.path = path;
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						//Gdx.app.log(TAG, "foundsystem "  + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] );

						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
						if (path.size == 0){
							parent.e.target.set(parent.e.x, parent.e.y);
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						} else {
							parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						}
						return;
					}
					
					
					break;
				
					
					
				case EntityAI.FIX:{
					int smallDist = 1000000;
					int distIndex = -1;
					for (int k = 0; k < parent.e.fixOrder.length; k++){
						int fixSystem = parent.e.fixOrder[k];
						IntIntMap damaged = parent.e.ship.map.damaged[fixSystem];
						Iterator<Entry> iter = damaged.iterator();
						while (iter.hasNext()){
							Entry ent = iter.next();
							int ind = ent.key;
							int x = ind % parent.e.ship.mapWidth;
							int y = ind / parent.e.ship.mapWidth;
							
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);
							if (dist < smallDist && ( block & Ship.BLOCK_DATA_MASK ) == 0 && !parent.e.ship.isReserved(x, y)){
								smallDist = dist;
								distIndex = ind;
							}
							//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  ");
							
						}
						
						if (distIndex == -1) break;
						
						int targetX = distIndex % parent.e.ship.mapWidth;
						int targetY = distIndex / parent.e.ship.mapWidth;
						path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, targetX, targetY);
						boolean fixed = (m.get(parent.e.x, parent.e.y) & Ship.BLOCK_DAMAGE_MASK) == 0;
						if (path.size == 0 && fixed ){
							Pools.free(path);
							path = null;
							//Gdx.app.log(TAG, "0 path ");// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);

							break;
						}
						if (path == null){
							Gdx.app.log(TAG, "NULL PATH");
							return;
						}
						parent.e.path = path;
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
						if (path.size == 0){
							parent.e.target.set(parent.e.x, parent.e.y);
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						} else {
							parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						}
						return;
						
					}
				}
					
					break;
				case EntityAI.FIRE:{

					int smallDist = 1000000;
					int distIndex = -1;
					//for (int k = 0; k < parent.e.fixOrder.length; k++)
					IntIntMap lista = parent.e.ship.map.onFire;
					if (lista.size > 0)
					{
						
						Iterator<Entry> iter = lista.iterator();
						while (iter.hasNext()){
							Entry ent = iter.next();
							int ind = ent.key;
							int x = ind % parent.e.ship.mapWidth;
							int y = ind / parent.e.ship.mapWidth;
							
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);
							if (dist < smallDist && ( block & Ship.BLOCK_DATA_MASK ) == 0 && !parent.e.ship.isReserved(x, y)){
								smallDist = dist;
								distIndex = ind;
							}
							//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  ");
							
						}
						
						if (distIndex == -1) break;
						
						int targetX = distIndex % parent.e.ship.mapWidth;
						int targetY = distIndex / parent.e.ship.mapWidth;
						path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, targetX, targetY);
						boolean notOnFire = (m.get(parent.e.x, parent.e.y) & Ship.BLOCK_FIRE_MASK) == 0;
						if (path.size == 0 && notOnFire ){
							Pools.free(path);
							path = null;
							//Gdx.app.log(TAG, "0 path ");// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);

							break;
						}
						if (path == null){
							Gdx.app.log(TAG, "NULL PATH");
							return;
						}
						parent.e.path = path;
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
						if (path.size == 0){
							parent.e.target.set(parent.e.x, parent.e.y);
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						} else {
							parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						}
						return;
						
					}
				
				}
					
					break;
				case EntityAI.WANDER:
					int targetX = parent.e.x, targetY = parent.e.y;
					targetX += MathUtils.random(-10, 10);
					targetY += MathUtils.random(-10, 10);
					targetX = Math.max(Math.min(targetX,  parent.e.ship.mapHeight-2), 1);
					targetY = Math.max(Math.min(targetY,  parent.e.ship.mapHeight-2), 1);
					path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, targetX, targetY);
					
					if (path.size == 0){
						Pools.free(path);
						path = null;
						return;
					} else {
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						//Gdx.app.log(TAG, "found"  + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] );
						parent.e.path = path;
						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
						//parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
					}
					return;
				}
			}
			/*parent.e.actionIndexForPath = parent.e.buttonOrder[actionIndexForPath ];
			AFollowPath follow = Pools.obtain(AFollowPath.class);
			addBeforeMe(follow);
			isFinished = true;
			
			if (path.size == 0){
				parent.e.target.set(parent.e.x, parent.e.y);
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
			} else {
				parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
				
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
			}*/
			
		}
		
	}

	private int getBlockID(int action) {
		int blockID = 0;
		switch (action){
		case EntityAI.SHIELDS:blockID = Ship.SHIELD; break;
		case EntityAI.ENGINE:blockID = Ship.ENGINE; break;
		case EntityAI.OXYGEN:blockID = Ship.OXYGEN; break;
		case EntityAI.POWER:blockID = Ship.POWER; break;
		case EntityAI.WEAPON:blockID = Ship.WEAPON; break;
		}
		return blockID;
	}
	private int getDistanceTo(int x, int y, Ship map) {
		
		return Math.abs(parent.e.x - x) + Math.abs(parent.e.y - y);
	}
	@Override
	public void onEnd(World world, Ship map) {
		//Gdx.app.log(TAG, "finished " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] );
		if (parent.e.path == null) throw new GdxRuntimeException("null path " + parent.e.actionIndexForPath);
	}

	
	@Override
	public void onStart(World world, Ship map) {
		hasStartedPath = false;
	}

}
