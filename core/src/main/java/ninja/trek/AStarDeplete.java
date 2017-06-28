package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.BinaryHeap.Node;
import com.badlogic.gdx.utils.IntArray;

/** @author Nathan Sweet */
public class AStarDeplete {
	private static final String TAG = "astar";
	private final int width, height;
	private final BinaryHeap<PathNode> open;
	private final PathNode[] nodes;
	int runID;
	private final IntArray path = new IntArray();
	//private int targetX, targetY;
	private Ship ship;
	private int damagePending;
	private int targetID;
	
	public AStarDeplete (int width, int height, Ship ship) {
		this.width = width;
		this.height = height;
		open = new BinaryHeap(width * 4, false);
		nodes = new PathNode[width * height];
		this.ship = ship;
	}

	/** Returns x,y pairs that are the path from the target to the start. */
	public int getPath (int startX, int startY, int target, int damage) {
		damagePending = damage;
		path.clear();
		open.clear();
		targetID = target;
		
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
		while (open.size > 0) {
			PathNode node = open.pop();
			
			checkBlock(node);
				
			if (damagePending == 0)return damagePending;
			
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
		
		return damagePending;
	}

	private void checkBlock(PathNode node) {
//		return getBlockSmallestActionIndex(node, goals) == 0;
		//return false;
		
		int block = ship.map.get(node.x, node.y);
		int depletion = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
		int boost =	(block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		boost += 1;//using as a multiplier
		int maxToAdd = ship.maxDepletionBySystem[block & Ship.BLOCK_ID_MASK] * boost - depletion;
		
		int toAdd = Math.min(maxToAdd, damagePending);
		if (toAdd == 0) return;
		depletion += toAdd/boost;
		//Gdx.app.log(TAG, "DEPLETE " + depletion);
		damagePending -= toAdd;
		block = block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_AIR_MASK);
		block |= depletion << Ship.BLOCK_DATA_BITS;
		ship.map.set(node.x, node.y, block);
		ship.map.needsBoost[block & Ship.BLOCK_ID_MASK].put(node.x + node.y * width, 0);
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
		int block = ship.map.get(x, y) & Ship.BLOCK_ID_MASK;
		if (block == targetID)
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