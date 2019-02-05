package ninja.trek.actions;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
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
import ninja.trek.entity.Entity;


public class AWaitForPath extends Action {

	private static final String TAG = "path wait a"
			;
	private Array<GridPoint2> candidates = new Array<GridPoint2>();
	public GridPoint2 to = new GridPoint2();
	private boolean hasStartedPath;
	private IntArray path, found = new IntArray();
	private static PosIndexComparator posIndexComparator = new PosIndexComparator();
	private static FireComparator fireComparator = new FireComparator();

	public AWaitForPath() {
		lanes = LANE_ACTING;
		isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		if (!hasStartedPath){
			hasStartedPath = true;
		}
		if (hasStartedPath){
			//path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, parent.e.buttonOrder, parent.e.fixOrder);
			//parent.e.path = path;
			//Gdx.app.log(TAG, "path for " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] + "  size" + path.size);
			//Gdx.app.log(TAG, "found path, size " + path.size + " to " + to + parent.e.actionIndexForPath);
			int actionIndexForPath = 0;

			//Gdx.app.log(TAG, "update " + parent.e);
			for (int i = 0; i < parent.e.buttonOrder.length; i++){
				int action = parent.e.buttonOrder[i];
				if (parent.e.disabledButton[action]) continue;
				//Gdx.app.log(TAG, "action " + EntityAI.names[action]);
				int blockID = getBlockID(action);
				IntPixelMap m = parent.e.ship.map;
				IntIntMap list = getList(action, blockID, m);
				int[] fixOrder;
				switch (action){
				
				case Entity.FIRE://Gdx.app.log(TAG, "look fire " + list.size);
					if (process(list, map, action, false)) return;
					break;
				case Entity.ENGINE:
				case Entity.OXYGEN:
				case Entity.DRONE:
				case Entity.WEAPON:
				case Entity.SHIELDS:
				case Entity.TELEPORTER:
				case Entity.SCIENCE:
					
					//found.clear();
					if (process(list, map, action, true)) return;
					break;
				case Entity.FIX:
					fixOrder = parent.e.fixOrder;
					for (int h = 0; h < fixOrder.length; h++)
					{
						list = m.damaged[fixOrder[h]];

						if (list == null) continue;
						//Gdx.app.log(TAG, "try fix " + h + " = " + fixOrder[h] );
						if (process(list, map, action, false)) return;

					}
					
					break;
				case Entity.WANDER:
					int targetX = parent.e.x, targetY = parent.e.y;
					int tX = 0, tY = 0; 
					int biggestAir = -1;;
					for (int k = 0; k < 2; k++){
						targetX = parent.e.x + MathUtils.random(-10, 10);
						targetY = parent.e.y + MathUtils.random(-10, 10);
						targetX = Math.max(Math.min(targetX,  parent.e.ship.mapHeight-2), 1);
						targetY = Math.max(Math.min(targetY,  parent.e.ship.mapHeight-2), 1);
						int b = parent.e.ship.map.get(targetX,  targetY);
						int id = (b & Ship.BLOCK_ID_MASK);
						int air = b & Ship.BLOCK_AIR_MASK;
						if (air > biggestAir && id != Ship.WALL && id != Ship.VACCUUM){
							biggestAir = air;
							tX = targetX;
							tY = targetY;
						}
					}if (biggestAir != -1){
						path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, tX, tY);
						
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
							//parent.e.ship.reserve(parent.e.missileTarget.x, parent.e.missileTarget.y);
						}
					}
					return;
				}
			}
			/*parent.e.actionIndexForPath = parent.e.buttonOrder[actionIndexForPath ];
			AFollowPath follow = Pools.obtain(AFollowPath.class);
			addBeforeMe(follow);
			isFinished = true;
			
			if (path.size == 0){
				parent.e.missileTarget.set(parent.e.x, parent.e.y);
				parent.e.ship.reserve(parent.e.missileTarget.x, parent.e.missileTarget.y);
			} else {
				parent.e.missileTarget.set(parent.e.path.get(0), parent.e.path.get(1));
				
				parent.e.ship.reserve(parent.e.missileTarget.x, parent.e.missileTarget.y);
			}*/
		}
	}
	/*
		return true if it has found a path
	 */
	private boolean process(IntIntMap list, Ship map, int action, boolean requireBoost) {
		IntPixelMap m = parent.e.ship.map;
		if (list.size != 0){
			candidates.clear();
			int startRoom = map.room.get(parent.e.x, parent.e.y);
			//Gdx.app.log(TAG, "STARTRTARTSDGTDFSFD " + startRoom );
			Iterator<Entry> iter = list.iterator();
			while (iter.hasNext()){
				Entry ent = iter.next();
				int ind = ent.key;
				int x = ind % m.width;
				int y = ind / m.width;
				int dist = getDistanceTo(x, y, map);
				int block = m.get(x, y);
				int room = map.room.get(x, y);
				if (room < 0) {
					Gdx.app.log(TAG, "missileTarget room invalid " + Ship.systemNames[(block & Ship.BLOCK_ID_MASK)]);
				}
				if (startRoom < 0) {
					Gdx.app.log(TAG, "start room invalid " +x+"," +y);
				}
				if (!map.roomsConnected[startRoom][room]){
					//Gdx.app.log(TAG, "rooms not connected " + startRoom + " , " + room);
				}
				boolean boostValid =  true;
				if (!map.roomsConnected[startRoom][room]){
					//Gdx.app.log(TAG, "rooms not connected " +room+"," +startRoom);
				}
				if (requireBoost) boostValid = ((block & Ship.BLOCK_DATA_MASK)  == 0);

				if (
						boostValid && !parent.e.ship.isReserved(x, y)
								//&& startRoom >= 0 && room >= 0
								&&
								map.roomsConnected[startRoom][room]



						){
					GridPoint2 newEnt = Pools.obtain(GridPoint2.class);
					newEnt.set(x, y);
					candidates.add(newEnt);
					//Gdx.app.log(TAG, "add candidate " + x + ", " + y + "  " + ent.key);
				}
			}
			Comparator comparator = null;
			if (action == Entity.FIRE){
				fireComparator.set(parent.e.x, parent.e.y, parent.e.ship);
				comparator = fireComparator;
			} else {
				posIndexComparator.set(parent.e.x, parent.e.y);
				comparator = posIndexComparator;

			}
			candidates.sort(comparator);

			if (candidates.size == 0) return false;
			boolean hasFoundCandidate = false;
			int candidateI = 0, pathCount = 0;
			while (!hasFoundCandidate && candidateI < candidates.size && pathCount < 5){
				GridPoint2 target = candidates.get(candidateI);
				if (target.x == parent.e.x && target.y == parent.e.y){
					candidateI++;
					//Gdx.app.log(TAG, "early skip candidate " + candidateI + "  " + candidates.size );
					continue;
				}
				//Gdx.app.log(TAG, "getting path");
				path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, target.x, target.y);
				pathCount++;
				if (path.size != 0){
					hasFoundCandidate = true;
					//Gdx.app.log(TAG, "found candidate " + parent.e.x + ", " + parent.e.y + ", " + path.get(0) + ", " + path.get(1));
				} else {
					//Gdx.app.log(TAG, "0  candidate " + candidateI );
				}
				candidateI++;
			}
			if (!hasFoundCandidate){
				//Gdx.app.log(TAG, "found NO candidate " + parent.e.glyph + candidates.size +  " c " + candidateI);
				return false;
			}
			parent.e.path = path;
			parent.e.actionIndexForPath = action;
			AFollowPath follow = Pools.obtain(AFollowPath.class);
			addBeforeMe(follow);
			isFinished = true;
			//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
			if (path.size == 0){
				//Gdx.app.log(TAG, "0 PATH");
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
				throw new GdxRuntimeException("fdjk2)");
				//parent.e.missileTarget.set(targetX, targetY);
			} else {
				parent.e.target.set(path.get(0), path.get(1));
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
			}

			return true;
		}
		return false;
	}

	private IntIntMap getList(int action, int blockID, IntPixelMap m) {
		switch (action){
		
		case Entity.FIRE:
			return m.onFire;
		case Entity.ENGINE:
		case Entity.OXYGEN:
		case Entity.DRONE:
		case Entity.WEAPON:
		case Entity.SHIELDS:
		case Entity.TELEPORTER:
		case Entity.SCIENCE:return m.needsBoost[blockID];
		}
		return null;
		
	}
	private int getBlockID(int action) {
		int blockID = 0;
		switch (action){
		case Entity.SHIELDS:blockID = Ship.SHIELD; break;
		case Entity.ENGINE:blockID = Ship.ENGINE; break;
		case Entity.OXYGEN:blockID = Ship.OXYGEN; break;
		case Entity.DRONE:blockID = Ship.DRONE; break;
		case Entity.WEAPON:blockID = Ship.WEAPON; break;
		case Entity.TELEPORTER:blockID = Ship.TELEPORTER; break;
		case Entity.SCIENCE:blockID = Ship.SCIENCE; break;
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

		int currentRoom = parent.e.ship.room.get(parent.e.x, parent.e.y);
		//Gdx.app.log(TAG, "found path " + currentRoom + "  " + parent.e.missileTarget);
	}

	
	@Override
	public void onStart(World world, Ship map) {
		hasStartedPath = false;
		//Gdx.app.log(TAG, "start");
	}
	
	private static class PosIndexComparator implements Comparator<GridPoint2>{
		private int x;
		private int y, ox, oy, ox2, oy2;
		
		public void set(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int compare(GridPoint2 o1, GridPoint2 o2) {
			
			return (Math.abs(x - o1.x) + Math.abs(y - o1.y)) 
					-
					(Math.abs(x - o2.x) + Math.abs(y - o2.y))
					;
		}
		
	}

	private static class FireComparator implements Comparator<GridPoint2>{
		private int x;
		private int y, ox, oy, ox2, oy2;
		private Ship ship
				;

		public void set(int x, int y, Ship ship){
			this.x = x;
			this.y = y;
			this.ship = ship;
		}

		@Override
		public int compare(GridPoint2 o1, GridPoint2 o2) {

			int block1 = ship.map.get(o1.x, o1.y);
			int block2 = ship.map.get(o2.x, o2.y);
			int air1 = (block1 & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			int air2 = (block2 & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			air1 /= 8;
			air2 /= 8;
			int d = (Math.abs(x - o1.x) + Math.abs(y - o1.y) - air1)
					-
					(Math.abs(x - o2.x) + Math.abs(y - o2.y) - air2);

			return d
					;
		}

	}

}
