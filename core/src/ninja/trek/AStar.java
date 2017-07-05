package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.BinaryHeap.Node;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;

/** @author Nathan Sweet */
public class AStar {
	private static final String TAG = "astar";
	private static final int MAX_ITERATIONS = 10000000;
	private final int width, height;
	private final BinaryHeap<PathNode> open;
	private final PathNode[] nodes;
	int runID;
	private IntArray path;// = new IntArray();
	//private int targetX, targetY;
	private Ship ship;
	private PathNode goalNode;
	private int goalIndex, goalFixIndex;
	public AStar (int width, int height, Ship ship) {
		this.width = width;
		this.height = height;
		open = new BinaryHeap(width * 4, false);
		nodes = new PathNode[width * height];
		this.ship = ship;
	}

	/** Returns x,y pairs that are the path from the target to the start. */
	public IntArray getPath (int startX, int startY, int[] goals, int[] fixOrder) {
		path = Pools.obtain(IntArray.class);
		path.clear();
		open.clear();
		goalIndex = goals.length;
		goalNode = null;
		runID++;
		if (runID < 0) runID = 1;

		int index = startY * width + startX;
		PathNode root = nodes[index];
		if (root == null) {
			root = new PathNode(0);
			root.x = startX;
			root.y = startY;
			nodes[index] = root;
		}
		root.parent = null;
		root.pathCost = 0;
		open.add(root, 0);

		int lastColumn = width - 1, lastRow = height - 1;
		int i = 0;
		while (open.size > 0 && i < MAX_ITERATIONS) {
			PathNode node = open.pop();
			if (checkBlock(node, goals, fixOrder)){//node.x == targetX && node.y == targetY) {
				actionIndexForPath = getBlockSmallestActionIndex(node, goals, fixOrder);
				while (node != root) {
					path.add(node.x);
					path.add(node.y);
					node = node.parent;
				}
				return path;
			}
			node.closedID = runID;
			int x = node.x;
			int y = node.y;
			if (x < lastColumn) {
				addNode(node, x + 1, y, 10);
				//if (y < lastRow) addNode(node, x + 1, y + 1, 14); // Diagonals cost more, roughly equivalent to sqrt(2).
				//if (y > 0) addNode(node, x + 1, y - 1, 14);
			}
			if (x > 0) {
				addNode(node, x - 1, y, 10);
				//if (y < lastRow) addNode(node, x - 1, y + 1, 14);
				//if (y > 0) addNode(node, x - 1, y - 1, 14);
			}
			if (y < lastRow) addNode(node, x, y + 1, 10);
			if (y > 0) addNode(node, x, y - 1, 10);
			i++;
		}
		PathNode node = goalNode;
		if (node == null){
			//Gdx.app.log(TAG, "NULL" + path.size);
			return null;//path;
		}
		actionIndexForPath = getBlockSmallestActionIndex(node, goals, fixOrder);
		if (actionIndexForPath == -1) return null;
		while (node != root) {
			path.add(node.x);
			path.add(node.y);
			node = node.parent;
		}
		return path;
	}
	public int actionIndexForPath;

	private boolean checkBlock(PathNode node, int[] goals, int[] fixOrder) {
		return getBlockSmallestActionIndex(node, goals, fixOrder) == 0;
		//return false;
	}

	private int getBlockSmallestActionIndex(PathNode node, int[] goals, int[] fixOrder) {
		if (ship.isReserved(node.x, node.y)) return -1;
		int block = ship.map.get(node.x, node.y);
		boolean found = false;
		for (int i = 0; i < goals.length; i++){
			switch (goals[i]){
			case EntityAI.FIX:
				int damage = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
				if (damage > 0)
					found = true;
				
				break;
			case EntityAI.SHIELDS:
				if ((block & Ship.BLOCK_ID_MASK) == Ship.SHIELD){
					int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
					if (currentFire == 0 && currentDam == 0 && currentBoost == 0 && currentDep == 0)
						found = true;
				}
				break;
			case EntityAI.OXYGEN:
				if ((block & Ship.BLOCK_ID_MASK) == Ship.OXYGEN){
					int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
					if (currentFire == 0 && currentDam == 0 && currentBoost == 0 && currentDep == 0)
						found = true;
				}
				break;
			case EntityAI.DRONE:
				if ((block & Ship.BLOCK_ID_MASK) == Ship.DRONE){
					int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
					if (currentFire == 0 && currentDam == 0 && currentBoost == 0 && currentDep == 0)
						found = true;
				}
				break;
			case EntityAI.WEAPON:
				if ((block & Ship.BLOCK_ID_MASK) == Ship.WEAPON){
					int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
					if (currentFire == 0 && currentDam == 0 && currentBoost == 0 && currentDep == 0)
						found = true;
				}
				break;
			case EntityAI.ENGINE:
				if ((block & Ship.BLOCK_ID_MASK) == Ship.ENGINE){
					int currentBoost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
					int currentFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
					int currentDam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
					int currentDep = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
					if (currentFire == 0 && currentDam == 0 && currentBoost == 0 && currentDep == 0)
						found = true;
				}
				break;
			case EntityAI.FIRE:
				if (((block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS)== 1)
				
				{
					found = true;
					//Gdx.app.log(TAG, "fire " + (block&Ship.BLOCK_ID_MASK)
					//		  + " " + ((block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS));
				}
				break;
			
			case EntityAI.SHOOT: break;
			case EntityAI.WANDER: break;
			
			}
			if (found){
				//if (i == 0) return 0;
				
				if (i < goalIndex){
					goalIndex = i;
					goalNode = node;
					if (goals[goalIndex] == EntityAI.FIX)
						goalFixIndex = getFixIndex(fixOrder, block);;
				}  
				 else if (i == goalIndex){
					 if (goals[goalIndex] == EntityAI.FIX){
							int fixIndex = getFixIndex(fixOrder, block);
							if (fixIndex < goalFixIndex){
								goalFixIndex = fixIndex;
								goalIndex = i;
								goalNode = node;
							}
					 }
					 else if (goalNode.pathCost > node.pathCost){
						goalIndex = i;
						goalNode = node;
						
					 }
				}
				
				if (goalNode != null) return goalIndex; else return -1;
			}
		}
		return -1;
	}

	private int getFixIndex(int[] fixOrder, int block) {
		block &= Ship.BLOCK_ID_MASK;
		for (int i = 0; i < fixOrder.length; i++){
			if (block == fixOrder[i]) return i;
		}
		return 110;
	}

	private void addNode (PathNode parent, int x, int y, int cost) {
		if (!isValid(x, y)) return;

		int pathCost = parent.pathCost + cost;
		float score = pathCost + g(x, y);

		int index = y * width + x;
		PathNode node = nodes[index];
		if (node != null && node.runID == runID) { // Node already encountered for this run.
			if (node.closedID != runID && pathCost < node.pathCost) { // Node isn't closed and new cost is lower.
				// Update the existing node.
				open.setValue(node, score);
				node.parent = parent;
				node.pathCost = pathCost;
			}
		} else {
			// Use node from the cache or create a new one.
			if (node == null) {
				node = new PathNode(0);
				node.x = x;
				node.y = y;
				nodes[index] = node;
			}
			open.add(node, score);
			node.runID = runID;
			node.parent = parent;
			node.pathCost = pathCost;
		}
	}
	
	private int g(int x, int y) {
		return 0;//
		//return Math.abs(x - targetX) + Math.abs(y - targetY);
	}

	protected boolean isValid (int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return false;
		int block = ship.map.get(x, y);
		if (block != Ship.WALL)
			return true;
		return false;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	static private class PathNode extends Node {
		int runID, closedID, x, y, pathCost;
		PathNode parent;

		public PathNode (float value) {
			super(value);
		}
	}

	public IntArray process() {
		
		return null;
	}
}