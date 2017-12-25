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

public class AWaitForPath extends Action {

	private static final String TAG = "path wait a"
			;
	private Array<GridPoint2> candidates = new Array<GridPoint2>();
	public GridPoint2 to = new GridPoint2();
	private boolean hasStartedPath;
	private IntArray path, found = new IntArray();
	private static PosIndexComparator posIndexComparator = new PosIndexComparator();;
	
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
			IntPixelMap m = parent.e.ship.map;
			//Gdx.app.log(TAG, "update " + parent.e);
			for (int i = 0; i < parent.e.buttonOrder.length; i++){
				int action = parent.e.buttonOrder[i];
				if (parent.e.disabledButton[action]) continue;
				//Gdx.app.log(TAG, "action " + EntityAI.names[action]);
				int blockID = getBlockID(action);
				IntIntMap list = getList(action, blockID, m);
				int[] fixOrder;
				switch (action){
				
				case EntityAI.FIRE://Gdx.app.log(TAG, "look fire " + list.size);
					if (list.size != 0){
						//if (list.nextClearBit(0) == -1) return;
						candidates.clear();
						int startRoom = map.room.get(parent.e.x, parent.e.y);
						Iterator<Entry> iter = list.iterator();
						while (iter.hasNext()){
							Entry ent = iter.next();
							int ind = ent.key;
							int x = ind % m.width;
							int y = ind / m.width;
							int dist = getDistanceTo(x, y, map);
							///Gdx.app.log(TAG, "look fire " + dist);
							int block = m.get(x, y);
							int room = map.room.get(x, y);
							if (
									 !parent.e.ship.isReserved(x, y)
											//&& startRoom >= 0 && map.roomsConnected[startRoom][room]
									){
								GridPoint2 newEnt = Pools.obtain(GridPoint2.class);
								newEnt.set(x, y);
								candidates.add(newEnt);
								//
								// Gdx.app.log(TAG, "add candidate " + x + ", " + y + "  " + ent.key);
							}
						}
						posIndexComparator.set(parent.e.x, parent.e.y);
						candidates.sort(posIndexComparator);

						if (candidates.size == 0) break;
						boolean hasFoundCandidate = false;
						int candidateI = 0;
						while (!hasFoundCandidate && candidateI < candidates.size){
							GridPoint2 target = candidates.get(candidateI);
							if (target.x == parent.e.x && target.y == parent.e.y){
								candidateI++;
								//Gdx.app.log(TAG, "early skip candidate " + candidateI + "  " + candidates.size );
								continue;
							}
							path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, target.x, target.y);
							if (path.size != 0){
								hasFoundCandidate = true;
								//Gdx.app.log(TAG, "found candidate " + parent.e.x + ", " + parent.e.y + ", " + path.get(0) + ", " + path.get(1));
							} else {
								//Gdx.app.log(TAG, "0 found candidate " );
							}
							candidateI++;
						}
						if (!hasFoundCandidate){
							//Gdx.app.log(TAG, "found NO candidate " + parent.e.glyph + candidates.size +  " c " + candidateI);
							break;
						}
						parent.e.path = path;
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
						if (path.size == 0){
							Gdx.app.log(TAG, "0 PATH");
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
							throw new GdxRuntimeException("fdjk2)");
							//parent.e.target.set(targetX, targetY);
						} else {
							parent.e.target.set(path.get(0), path.get(1));
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						}
						return;
					}
					break;
				case EntityAI.ENGINE:
				case EntityAI.OXYGEN:
				case EntityAI.DRONE:
				case EntityAI.WEAPON:
				case EntityAI.SHIELDS:
				case EntityAI.TELEPORTER:
				case EntityAI.SCIENCE:
					
					//found.clear();
					if (list.size != 0){
						//if (list.nextClearBit(0) == -1) return;
						candidates.clear();
                        //map.calculateConnectivity();
						int startRoom = map.systemRooms.get(parent.e.x, parent.e.y);
					    Gdx.app.log(TAG, "STARTRTARTSDGTDFSFD " + startRoom );
						Iterator<Entry> iter = list.iterator();
						while (iter.hasNext()){
							Entry ent = iter.next();
							int ind = ent.key;
							int x = ind % m.width;
							int y = ind / m.width;
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);
							int room = map.room.get(x, y);
							if (
									( block & Ship.BLOCK_DATA_MASK ) == 0 && !parent.e.ship.isReserved(x, y)
									//&& startRoom >= 0 && map.roomsConnected[startRoom][room]
									){
								GridPoint2 newEnt = Pools.obtain(GridPoint2.class);
								newEnt.set(x, y);
								candidates.add(newEnt);
								//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  " + ent.key);
							}
						}
						posIndexComparator.set(parent.e.x, parent.e.y);
						candidates.sort(posIndexComparator);
						
						if (candidates.size == 0) break;
						boolean hasFoundCandidate = false;
						int candidateI = 0;
						while (!hasFoundCandidate && candidateI < candidates.size){
							GridPoint2 target = candidates.get(candidateI);
							if (target.x == parent.e.x && target.y == parent.e.y){
								candidateI++;
								//Gdx.app.log(TAG, "early skip candidate " + candidateI + "  " + candidates.size );
								continue;
							}
							path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, target.x, target.y);
							if (path.size != 0){
								hasFoundCandidate = true;
								//Gdx.app.log(TAG, "found candidate " + parent.e.x + ", " + parent.e.y + ", " + path.get(0) + ", " + path.get(1));
							} else {
								//Gdx.app.log(TAG, "0 found candidate " );
							}
							candidateI++;
						}
						if (!hasFoundCandidate){
							//Gdx.app.log(TAG, "found NO candidate " + parent.e.glyph + candidates.size +  " c " + candidateI);
							break;
						}
						parent.e.path = path;
						parent.e.actionIndexForPath = parent.e.buttonOrder[i];
						AFollowPath follow = Pools.obtain(AFollowPath.class);
						addBeforeMe(follow);
						isFinished = true;
						//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
						if (path.size == 0){
							Gdx.app.log(TAG, "0 PATH");
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
							throw new GdxRuntimeException("fdjk2)");
							//parent.e.target.set(targetX, targetY);
						} else {
							parent.e.target.set(path.get(0), path.get(1));
							parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
						}
						return;
					}
					break;
				case EntityAI.FIX:
					fixOrder = parent.e.fixOrder;
					for (int h = 0; h < fixOrder.length; h++)
					{
						//Gdx.app.log(TAG, "damaged " + h + " = " + fixOrder[h] +( m.damaged[2] == null));
						list = m.damaged[fixOrder[h]];
						if (list == null) continue;
						if (list.size != 0){
							//if (list.nextClearBit(0) == -1) return;
							candidates.clear();
							Iterator<Entry> iter = list.iterator();
							while (iter.hasNext()){
								Entry ent = iter.next();
								int ind = ent.key;
								int x = ind % m.width;
								int y = ind / m.width;
								int dist = getDistanceTo(x, y, map);
								int block = m.get(x, y);
								if (!parent.e.ship.isReserved(x, y)){
									GridPoint2 newEnt = Pools.obtain(GridPoint2.class);
									newEnt.set(x, y);
									candidates.add(newEnt);
									//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  " + ind);
								}
							}
							posIndexComparator.set(parent.e.x, parent.e.y);
							candidates.sort(posIndexComparator);
							
							if (candidates.size == 0) break;
							boolean hasFoundCandidate = false;
							int candidateI = 0;
							while (!hasFoundCandidate && candidateI < candidates.size){
								GridPoint2 target = candidates.get(candidateI);
								if (target.x == parent.e.x && target.y == parent.e.y){
									candidateI++;
									//Gdx.app.log(TAG, "early skip candidate " + candidateI + "  " + candidates.size );
									continue;
								}
								path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, target.x, target.y);
								if (path.size != 0){
									hasFoundCandidate = true;
									//Gdx.app.log(TAG, "found candidate " + parent.e.x + ", " + parent.e.y + ", " + path.get(0) + ", " + path.get(1));
								} else {
									//Gdx.app.log(TAG, "0 found candidate " +target );
								}
								candidateI++;
							}
							if (!hasFoundCandidate){
								//Gdx.app.log(TAG, "found NO candidate " + parent.e.glyph + candidates.size +  " c " + candidateI);
								break;
							}
							parent.e.path = path;
							parent.e.actionIndexForPath = parent.e.buttonOrder[i];
							AFollowPath follow = Pools.obtain(AFollowPath.class);
							addBeforeMe(follow);
							isFinished = true;
							//Gdx.app.log(TAG, "found" + parent.e);// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);
							if (path.size == 0){
								Gdx.app.log(TAG, "0 PATH");
								parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
								throw new GdxRuntimeException("fdjk2)");
								//parent.e.target.set(targetX, targetY);
							} else {
								parent.e.target.set(path.get(0), path.get(1));
								parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
							}
							return;
						}
					}
					
					break;
				case EntityAI.WANDER:
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
							//parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
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
				parent.e.target.set(parent.e.x, parent.e.y);
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
			} else {
				parent.e.target.set(parent.e.path.get(0), parent.e.path.get(1));
				
				parent.e.ship.reserve(parent.e.target.x, parent.e.target.y);
			}*/
		}
	}

	private IntIntMap getList(int action, int blockID, IntPixelMap m) {
		switch (action){
		
		case EntityAI.FIRE:
			return m.onFire;
		case EntityAI.ENGINE:
		case EntityAI.OXYGEN:
		case EntityAI.DRONE:
		case EntityAI.WEAPON:
		case EntityAI.SHIELDS:
		case EntityAI.TELEPORTER:
		case EntityAI.SCIENCE:return m.needsBoost[blockID];
		}
		return null;
		
	}
	private int getBlockID(int action) {
		int blockID = 0;
		switch (action){
		case EntityAI.SHIELDS:blockID = Ship.SHIELD; break;
		case EntityAI.ENGINE:blockID = Ship.ENGINE; break;
		case EntityAI.OXYGEN:blockID = Ship.OXYGEN; break;
		case EntityAI.DRONE:blockID = Ship.DRONE; break;
		case EntityAI.WEAPON:blockID = Ship.WEAPON; break;
		case EntityAI.TELEPORTER:blockID = Ship.TELEPORTER; break;
		case EntityAI.SCIENCE:blockID = Ship.SCIENCE; break;
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

}
