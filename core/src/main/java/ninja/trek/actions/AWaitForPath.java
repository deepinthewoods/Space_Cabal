package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.IntArray;
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
			Gdx.app.log(TAG, "update " + parent.e);
			for (int i = 0; i < parent.e.buttonOrder.length; i++){
				int action = parent.e.buttonOrder[i];
				Gdx.app.log(TAG, "action " + EntityAI.names[action]);
				int blockID = 0;
				switch (action){
				case EntityAI.SHIELDS:blockID = Ship.SHIELD; break;
				case EntityAI.ENGINE:blockID = Ship.ENGINE; break;
				case EntityAI.OXYGEN:blockID = Ship.OXYGEN; break;
				case EntityAI.POWER:blockID = Ship.POWER; break;
				case EntityAI.WEAPON:blockID = Ship.WEAPON; break;
				}
				switch (action){
				case EntityAI.ENGINE:
				case EntityAI.OXYGEN:
				case EntityAI.POWER:
				case EntityAI.WEAPON:
				case EntityAI.SHIELDS:
					Bits list = m.boosted[blockID];
					//found.clear();
					//Gdx.app.log(TAG, "STARTRTARTSDGTDFSFD " + list.length());
					
					if (list.nextClearBit(0) != -1){
						//if (list.nextClearBit(0) == -1) return;
						int startInd = list.nextClearBit(MathUtils.random(m.width * m.height));
						int ind = startInd;
						int smallDist = 1000000;
						int distIndex = -1;
						while (ind != -1 && ind < m.width * m.height){
							int x = ind % parent.e.ship.mapWidth;
							int y = ind / parent.e.ship.mapWidth;
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);

							if (dist < smallDist && !parent.e.ship.isReserved(x, y) && ( block & Ship.BLOCK_DATA_MASK ) == 0 ){
								smallDist = dist;
								distIndex = ind;
							}
							//Gdx.app.log(TAG, "look " + x + ", " + y + "  " + ind);
							ind = list.nextClearBit(ind+1);
						}
						ind = list.nextClearBit(0);
						while (ind != -1 && ind < startInd){
							int x = ind % parent.e.ship.mapWidth;
							int y = ind / parent.e.ship.mapWidth;
							
							int dist = getDistanceTo(x, y, map);
							int block = m.get(x, y);
							if (dist < smallDist && ( block & Ship.BLOCK_DATA_MASK ) == 0 && !parent.e.ship.isReserved(x, y)){
								smallDist = dist;
								distIndex = ind;
							}
							//Gdx.app.log(TAG, "lookb " + x + ", " + y + "  ");
							ind = list.nextClearBit(ind+1);
						}
						
						if (distIndex == -1) break;
						
						int targetX = distIndex % parent.e.ship.mapWidth;
						int targetY = distIndex / parent.e.ship.mapWidth;
						path = parent.e.ship.aStar.getPath(parent.e.x, parent.e.y, targetX, targetY);
						boolean boosted = (m.get(parent.e.x, parent.e.y) & Ship.BLOCK_BOOST_MASK) != 0;
						if (path.size == 0 && (list.get(parent.e.x + parent.e.y * m.width) || boosted)){
							Pools.free(path);
							path = null;
							Gdx.app.log(TAG, "0 path ");// + targetX + ", " + targetY + "  from " + parent.e.x + ", " + parent.e.y);

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
					
					
					break;
				
					
					
				case EntityAI.FIX:
					
					break;
				case EntityAI.FIRE:
					
					break;
				case EntityAI.WANDER:
					
					break;
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

	private int getDistanceTo(int x, int y, Ship map) {
		
		return Math.abs(parent.e.x - x) + Math.abs(parent.e.y - y);
	}
	@Override
	public void onEnd(World world, Ship map) {
		//Gdx.app.log(TAG, "finished");
	}

	
	@Override
	public void onStart(World world, Ship map) {
		hasStartedPath = false;
	}

}
