package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.EntityAI;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;

public class AWaitForPath extends Action {

	private static final String TAG = "path wait a"
			;
	public GridPoint2 to = new GridPoint2();
	private boolean hasStartedPath;
	private IntArray path;
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
			path = parent.e.map.aStar.getPath(parent.e.x, parent.e.y, parent.e.buttonOrder, parent.e.fixOrder);
			if (path != null) {
				//Gdx.app.log(TAG, "path for " + EntityAI.names[parent.e.buttonOrder[parent.e.actionIndexForPath]] + "  size" + path.size);
				//Gdx.app.log(TAG, "found path, size " + path.size + " to " + to + parent.e.actionIndexForPath);
				parent.e.path = path;
				parent.e.actionIndexForPath = parent.e.buttonOrder[parent.e.map.aStar.actionIndexForPath];
				AFollowPath follow = Pools.obtain(AFollowPath.class);
				addBeforeMe(follow);
				isFinished = true;
				if (path.size == 0){
					//Gdx.app.log(TAG, "0 path " + parent.e);
					//parent.clear();
					//parent.addToStart(Pools.obtain(ABase.class));
				}
			}
		}
		
	}

	@Override
	public void onEnd(World world, Ship map) {
		
	}

	
	@Override
	public void onStart(World world, Ship map) {
		hasStartedPath = false;
	}

}
