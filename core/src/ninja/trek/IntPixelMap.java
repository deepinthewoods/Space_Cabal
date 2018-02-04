package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pools;

public class IntPixelMap{
	private static final String TAG = "int pixel map";
	private static final int FIRE_THRESHOLD = 1;
	private static final int MIN_AIR_FOR_FIRE = 4;
	private static int[] updateOrder;
	private static int updateOrderLength;
	public static BlockDef[] defs;
	transient int[] map;
	public int width, height, chunksX, chunksY;
	;
	private boolean specialConstructor;
	private int updateProgress;
	private int updateRepeats;
	public GridPoint2 spawn = new GridPoint2();
	public transient IntIntMap[]
					//,
					damaged = new IntIntMap[Ship.systemNames.length];
	public transient IntIntMap onFire = new IntIntMap();
	//public Bits onFire = new Bits();
	public transient IntIntMap[] needsBoost = new IntIntMap[Ship.systemNames.length];
	static {
		Array<GridPoint2> upd = new Array<GridPoint2>();
		for (int i = 0; i < 64; i++){
			for (int j = 0; j < 64; j++){
				upd.add(new GridPoint2(i, j));
			}
		}
		upd.shuffle();
		updateOrder = new int[upd.size * 2];
		for (int i =0; i < upd.size; i++){
			updateOrder[i*2] = upd.get(i).x;
			updateOrder[i*2+1] = upd.get(i).y;				
		}
		updateOrderLength = updateOrder.length/2;
		defs = new BlockDef[16];
		for (int i = 0; i < defs.length; i++){
			defs[i] = new BlockDef();
		}
		defs[Ship.SHIELD] = new BlockDef();
		defs[Ship.OXYGEN] = new BlockDef(){

			@Override
			public void update(int x, int y, int block, IntPixelMap map, Ship ship) {
				//Gdx.app.log("jfdksl", "air update" + block);
				int dam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
				if (dam >= ship.damageThreshold[Ship.OXYGEN]) return;
				int depletion = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
				int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
				
				int neededAir = 127 - air;
				int maxAddedDepletion = ship.maxDepletionBySystem[block & Ship.BLOCK_ID_MASK] - depletion;
				int actualDepletion = Math.min(maxAddedDepletion, neededAir/4);
				//Gdx.app.log(TAG, ");
				depletion += actualDepletion;
				air += actualDepletion * 4;
				//if (maxAddedDepletion != 0)Gdx.app.log(TAG, "depl " + maxAddedDepletion);
				block = block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK);
				block |= depletion << Ship.BLOCK_DATA_BITS;
				//ship.map.set(x, y, block);
				
				block |= air << Ship.BLOCK_AIR_BITS;
				
				map.set(x, y, block);
				//Gdx.app.log("jfdksl", "air update after" + block);
			}
		};
		defs[Ship.VACCUUM] = new BlockDef(){
			@Override
			public void update(int x, int y, int block, IntPixelMap map, Ship ship) {
				//Gdx.app.log("jfdksl", "air update" + block);
				block = block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK);
				//block |= 63 << Ship.BLOCK_AIR_BITS;
				//block |= 2 << Ship.BLOCK_FIRE_BITS;
				map.set(x, y, block);
				//Gdx.app.log("jfdksl", "air update after" + block);
			}
		};
	}
	public IntPixelMap(){
		for (int i = 0; i < needsBoost.length; i++){
			needsBoost[i] = new IntIntMap();
			damaged[i] = new IntIntMap();
		}
	}
	public IntPixelMap(int w, int h){
		width = w;
		height = h;
		chunksX = width / Ship.CHUNKSIZE + 1;
		chunksY = height / Ship.CHUNKSIZE + 1;
		map = new int[chunksX * Ship.CHUNKSIZE * chunksY * Ship.CHUNKSIZE];

		for (int i = 0; i < 100; i++){
			//set(MathUtils.random(width), MathUtils.random(10), MathUtils.random(4));
		}
		updateProgress = 0;
		updateRepeats = (width * height) / updateOrder.length;
		
		for (int i = 0; i < needsBoost.length; i++){
			needsBoost[i] = new IntIntMap();
			damaged[i] = new IntIntMap();
		}
	}
	public IntPixelMap(IntPixelMap map) {
		this(map.width, map.height);
		specialConstructor = true;
		// Gdx.app.log(TAG, "ALTERNATE MAP ");
		for (int i = 0; i < 1000; i++){
			//set(MathUtils.random(width-1), MathUtils.random(height)-1, MathUtils.random(4));
		}
		for (int i = 0; i < needsBoost.length; i++){
			needsBoost[i] = new IntIntMap();
			damaged[i] = new IntIntMap();
		}
	}
	public int get(int x, int y) {
		//Gdx.app.log(TAG, "get " + x + ", " + y);
		if (x >= width || y >= height || x < 0 || y < 0) return Ship.VACCUUM;
		int blockIndex = (x) + (y) * chunksX * Ship.CHUNKSIZE;
		
		
		

		return map[blockIndex];
	}
	public void set(int x, int y, int b) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//int currentBoost = (map[x  + y * chunkSize * chunksX] & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		//if (currentBoost == 1) throw new GdxRuntimeException("Edkl)" + (b & Ship.BLOCK_BOOST_MASK));
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		map[x  + y * Ship.CHUNKSIZE * chunksX] = b;

	}
	public void damage(int x, int y, int dam, Ship ship) {
        if (x >= width || y >= height || x < 0 || y < 0) return ;
        //if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
        int index = x  + y * Ship.CHUNKSIZE * chunksX;
        int b = map[index];
        int id = b & Ship.BLOCK_ID_MASK;
        if (id == Ship.FLOOR) return;
        int currentDam = (b & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
        currentDam += dam;
        currentDam = Math.min(Ship.MAX_DAMAGE, currentDam);
        map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK) | (currentDam << Ship.BLOCK_DAMAGE_BITS);
        markDamagedIndex(x + y * width, id);
        ShipEntity se = ship.getShipEntity();
        if (se != null) se.health--;
        //skip boost
        needsBoost[id].remove(x + y * width, 0);
        //Gdx.app.log(TAG, "damage " + (b == map[x + y * chunkSize * chunksX]));
    }

    public int getDamage(int x, int y) {
        if (x >= width || y >= height || x < 0 || y < 0) return 0;
        //if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
        int index = x  + y * Ship.CHUNKSIZE * chunksX;
        int b = map[index];
        //int id = b & Ship.BLOCK_ID_MASK;
        //if (id == Ship.FLOOR) return;
        int currentDam = (b & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;

        return currentDam;
    }

	private void markDamagedIndex(int index, int id) {
		damaged[id].put(index, 0);
	}
	
	private void unmarkDamaged(int index, int id) {
		damaged[id].remove(index, 0);
		
	}
	public void fix(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		int index = x  + y * Ship.CHUNKSIZE * chunksX;
		int b = map[index];
		int id = b & Ship.BLOCK_ID_MASK;
		if (id == Ship.FLOOR) return;
		int currentDam = (b & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
		currentDam = 0;
		currentDam = Math.min(Ship.MAX_DAMAGE, currentDam);
		map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK) | (currentDam << Ship.BLOCK_DAMAGE_BITS);
		unmarkDamaged(x + y * width, id);
		//if ((b & Ship.BLOCK_DATA_MASK) == 0)
			//needsBoost[id].put(x + y * width, 0);
		//Gdx.app.log(TAG, "damage " + (b == map[x + y 
	}

    public void fix(int x, int y, int fix) {
        if (x >= width || y >= height || x < 0 || y < 0) return ;
        //if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
        int index = x  + y * Ship.CHUNKSIZE * chunksX;
        int b = map[index];
        int id = b & Ship.BLOCK_ID_MASK;
        if (id == Ship.FLOOR) return;
        int currentDam = (b & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
        currentDam -= fix;
        currentDam = Math.min(Ship.MAX_DAMAGE, currentDam);
        currentDam = Math.max(0, currentDam);
        map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK) | (currentDam << Ship.BLOCK_DAMAGE_BITS);
        if (currentDam == 0)unmarkDamaged(x + y * width, id);
        //if ((b & Ship.BLOCK_DATA_MASK) == 0)
        //needsBoost[id].put(x + y * width, 0);
        //Gdx.app.log(TAG, "damage " + (b == map[x + y
    }
	public void fightFire(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		int index = x  + y * Ship.CHUNKSIZE * chunksX;
		int b = map[index];
		int id = b & Ship.BLOCK_ID_MASK;

		//if ((b & Ship.BLOCK_ID_MASK) == Ship.FLOOR) return;
		int currentFire = (b & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
		currentFire = 0;
		//currentFire = Math.min(Ship.MAX_DAMAGE, currentFire);
		map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK) 
				//| (currentFire << Ship.BLOCK_FIRE_BITS)
				;
		onFire.remove(x + y * width, 0);
		//Gdx.app.log(TAG, "damage " + (b == map[x + y 
	}
	public void boost(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)
		//Gdx.app.log(TAG, "boost " + x + ", " + y);
		int index = x  + y * Ship.CHUNKSIZE * chunksX;
		int b = map[index];
		int id = b & Ship.BLOCK_ID_MASK;
		//if ((b & Ship.BLOCK_ID_MASK) == Ship.FLOOR) return;
		int currentBoost = (b & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		currentBoost = 1;
		//currentFire = Math.min(Ship.MAX_DAMAGE, currentFire);
		map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_FIRE_MASK) 
				| (currentBoost << Ship.BLOCK_BOOST_BITS)
				;
		//Gdx.app.log(TAG, "boost " + id); 
		needsBoost[id].remove(x + y * width, 0);
		
	}
	public void unBoost(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)
		//Gdx.app.log(TAG, "boost " + x + ", " + y);
		int index = x  + y * Ship.CHUNKSIZE * chunksX;
		int b = map[index];
		int id = b & Ship.BLOCK_ID_MASK;
		//if ((b & Ship.BLOCK_ID_MASK) == Ship.FLOOR) return;
		int currentBoost = (b & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		currentBoost = 0;
		//currentFire = Math.min(Ship.MAX_DAMAGE, currentFire);
		map[index] = b & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_FIRE_MASK) 
				| (currentBoost << Ship.BLOCK_BOOST_BITS)
				;
		//Gdx.app.log(TAG, "boost " + id); 
		needsBoost[id].put(x + y * width, 0);
		
	}
	public void boostAll() {
		//Gdx.app.log(TAG, "BOOST ALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				boost(x, y);
	}
	public void unBoostAll() {
		
			//Gdx.app.log(TAG, "BOOST ALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					unBoost(x, y);
		
	}
	public void setAirForNewGame() {
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int block = get(x, y);
				int id = (block * Ship.BLOCK_ID_MASK);
				if (id == Ship.VACCUUM) continue;
				int nBlock = block & ~Ship.BLOCK_AIR_MASK;
				nBlock |= (31 << Ship.BLOCK_AIR_BITS);
				set(x, y, nBlock);
			}
		
	}
	public void processFloodFill(IntPixelMap m, int nodeX, int nodeY, int target, int replacement, int stack){
		if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return;
		if (target == replacement) return;
		if ((m.get(nodeX, nodeY) & Ship.BLOCK_ID_MASK) != target) return;
		//if ((get(nodeX, nodeY) & Ship.BLOCK_ID_MASK) != target) return;
		if (stack > 4000) return;
		//			3. Set the color of node to replacement-color.
		set(nodeX, nodeY, replacement);
		addNode(nodeX, nodeY-1);
		addNode(nodeX, nodeY+1);
		addNode(nodeX-1, nodeY);
		addNode(nodeX+1, nodeY);
		//			5. Return.

	}
	private void addNode(int x, int y) {
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(x, y);
		floodOpen.add(pt);
	}
	private transient Array<GridPoint2> floodOpen = new Array<GridPoint2>();
	public void floodFill(IntPixelMap m, int nodeX, int nodeY, int target, int replacement){
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(nodeX, nodeY);
		floodOpen.add(pt);
		while (floodOpen.size > 0){
			GridPoint2 node = floodOpen.pop();
			processFloodFill(m, node.x, node.y, target, replacement, 0);
			Pools.free(node);
		}
	}
	Color tmpC = new Color();
	
	
	
	public void processFloodFillW(IntPixelMap m, int nodeX, int nodeY, int replacement, int stack){
		if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return;
		//if (target == replacement) return;
		
		int id = (m.get(nodeX, nodeY) & Ship.BLOCK_ID_MASK);
		if (id == Ship.WALL || id == Ship.VACCUUM || id == Ship.DOOR) return;
		if (get(nodeX, nodeY) != 0) return;
		//f ((get(nodeX, nodeY) & Ship.BLOCK_ID_MASK) != target) return;
		if (stack > 4000) return;
		//			3. Set the color of node to replacement-color.
		set(nodeX, nodeY, replacement);
		addNode(nodeX, nodeY-1);
		addNode(nodeX, nodeY+1);
		addNode(nodeX-1, nodeY);
		addNode(nodeX+1, nodeY);
		//			5. Return.

	}
	private void addNodeW(int x, int y) {
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(x, y);
		floodOpen.add(pt);
	}
	public void floodFillWalkable(IntPixelMap m, int nodeX, int nodeY, int replacement){
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(nodeX, nodeY);
		floodOpen.add(pt);
		while (floodOpen.size > 0){
			GridPoint2 node = floodOpen.pop();
			processFloodFillW(m, node.x, node.y, replacement, 0);
			Pools.free(node);
		}
	}
	public void processFloodFillS(IntPixelMap m, int nodeX, int nodeY, int target, int replacement, int stack){
		if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return;
		//if (target == replacement) return;
		int id = (m.get(nodeX, nodeY) & Ship.BLOCK_ID_MASK);
		int myid = (get(nodeX, nodeY));
		if (id != target) return;
		if (myid != -1){
			//Gdx.app.log(TAG, "f " + myid);
			return;
		}
		//if (id == Ship.WALL || id == Ship.VACCUUM) return;
		
		//if (get(nodeX, nodeY) != 0) return;
		//f ((get(nodeX, nodeY) & Ship.BLOCK_ID_MASK) != target) return;
		if (stack > 4000) return;
		//			3. Set the color of node to replacement-color.
		set(nodeX, nodeY, replacement);
		addNode(nodeX, nodeY-1);
		addNode(nodeX, nodeY+1);
		addNode(nodeX-1, nodeY);
		addNode(nodeX+1, nodeY);
		//			5. Return.

	}
	
	public void floodFillSystem(IntPixelMap m, int nodeX, int nodeY, int target, int replacement){
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(nodeX, nodeY);
		floodOpen.add(pt);
		while (floodOpen.size > 0){
			GridPoint2 node = floodOpen.pop();
			processFloodFillS(m, node.x, node.y, target, replacement, 0);
			Pools.free(node);
		}
	}
	
	public void processFloodFillNoVac(IntPixelMap m, int nodeX, int nodeY, int replacement, int stack){
		if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return;
		//if (target == replacement) return;
		int id = (m.get(nodeX, nodeY) & Ship.BLOCK_ID_MASK);
		//int myid = (get(nodeX, nodeY));
		if (id == Ship.VACCUUM) return;
		if (get(nodeX, nodeY) == replacement) return;
		set(nodeX, nodeY, replacement);
		addNode(nodeX, nodeY-1);
		addNode(nodeX, nodeY+1);
		addNode(nodeX-1, nodeY);
		addNode(nodeX+1, nodeY);
	}
	
	public void floodFillNonVaccuum(IntPixelMap m, int nodeX, int nodeY, int replacement){
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(nodeX, nodeY);
		floodOpen.add(pt);
		while (floodOpen.size > 0){
			GridPoint2 node = floodOpen.pop();
			processFloodFillNoVac(m, node.x, node.y, replacement, 0);
			Pools.free(node);
		}
	}
    /*
    returns amount of damage done
     */
	public int processfloodFillMissileDamage(int nodeX, int nodeY, int damage, int stack, Ship ship, int seed){
        if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return 0;
        //if (target == replacement) return;
        int id = (get(nodeX, nodeY) & Ship.BLOCK_ID_MASK);
        //int myid = (get(nodeX, nodeY));
        if (id == Ship.VACCUUM) return 0;
        int dam = getDamage(nodeX, nodeY);
        //if (dam == Ship.MAX_DAMAGE)
            //get(nodeX, nodeY) == replacement)
        //return;
        int pendingDam = Ship.MAX_DAMAGE - dam;
        pendingDam = Math.min(damage, pendingDam);
        damage(nodeX, nodeY, pendingDam, ship);
        int nseed = seed & 3;;
        nseed = MathUtils.random(3);
        if (randomFillTotalElements < randomFillSizeLimit)
            addNode(nodeX+DX[nseed%4], nodeY+DY[nseed%4]);
        nseed++;


        return pendingDam;
    }

    public void floodFillMissileDamage(int nodeX, int nodeY, int damage, Ship ship){
        GridPoint2 pt = Pools.obtain(GridPoint2.class);
        int seed = MathUtils.random(0, 100000);
        pt.set(nodeX, nodeY);
        floodOpen.add(pt);
        while (floodOpen.size > 0 && damage > 0){
            GridPoint2 node = floodOpen.pop();
            damage -= processfloodFillMissileDamage(node.x, node.y, damage, 0, ship, seed*=31);
            Pools.free(node);
        }
        while (floodOpen.size > 0) Pools.free(floodOpen.pop());
    }
	public static final int[] DX = {-1, 0, 1, 0}
							, DY = {0, 1, 0, -1};
	public int randomFillIterations, randomFillTriesLimit = 300, randomFillTotal
	, randomFillTotalElements, randomFillSizeLimit = 200;
	private int airUpdateReplaceIndex = 1;
	private boolean randomFillFire = false;
	boolean cleared;
	public void resetRandomFloodFill(int sizeLimit, int triesLimit) {
		randomFillIterations = 0;
		randomFillTriesLimit = triesLimit;
		randomFillTotal = 0;
		randomFillTotalElements = 0;
		randomFillSizeLimit = sizeLimit;
		randomFillFire = false;
	}
	public void randomFloodFill(IntPixelMap map2, int x, int y, int replacement, int seed, boolean propagateFire, Ship ship) {
		if (x >= width || y >= height || x < 0 || y < 0) return;
		int block = map2.get(x, y);
		int id = (block & Ship.BLOCK_ID_MASK);
		int myid = (get(x, y));
		if (myid == replacement)return;
		if (id == Ship.VACCUUM || id == Ship.WALL) {
			//randomFillIterations++;
			return;
		}
		if (randomFillIterations > randomFillTriesLimit) return;
		randomFillIterations++;
		int air = block & Ship.BLOCK_AIR_MASK;
		air >>= Ship.BLOCK_AIR_BITS;
		int fire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
		if (fire == 1) randomFillFire = propagateFire;
		randomFillTotal += air;
		randomFillTotalElements++;
		
		
		//int dy = ((seed>>16) & 1)*2-1;
		seed *= 31;
		int prog = 0;
		
		//Gdx.app.log(TAG, "ff  " + seed + "  id " + id + " dx " +  dx + " dy " + dy); 
		int nseed = seed & 3;;
		nseed = MathUtils.random(3);
		if (randomFillTotalElements < randomFillSizeLimit)
			randomFloodFill(map2, x+DX[nseed%4], y+DY[nseed%4], replacement, seed, propagateFire, ship);
		nseed++;
		
		if (randomFillTotalElements < randomFillSizeLimit)
			randomFloodFill(map2, x+DX[nseed%4], y+DY[nseed%4], replacement, seed, propagateFire, ship);
		nseed++;
		
		
		if (randomFillTotalElements < randomFillSizeLimit)
			randomFloodFill(map2, x+DX[nseed%4], y+DY[nseed%4], replacement, seed, propagateFire, ship);
		nseed++;
		
		
		if (randomFillTotalElements < randomFillSizeLimit)
			randomFloodFill(map2, x+DX[nseed%4], y+DY[nseed%4], replacement, seed, propagateFire, ship);
		nseed++;
		int nair = randomFillTotal / randomFillTotalElements ;
		set(x, y, replacement);
		int nBlock = block & ~Ship.BLOCK_AIR_MASK;
		nBlock &= ~Ship.BLOCK_FIRE_MASK;
		if (randomFillFire) {
			nair = Math.max(0,  nair/3);
			if (nair > MIN_AIR_FOR_FIRE){
				nBlock |= (1 << Ship.BLOCK_FIRE_BITS);
				map2.onFire.put(x + y * map2.width, 1);
			} else {
				map2.onFire.remove(x + y * map2.width, 0);
				nBlock &= ~((1 << Ship.BLOCK_FIRE_BITS));
			}
			nBlock |= (nair << Ship.BLOCK_AIR_BITS);
			map2.set(x, y, nBlock);
			map2.damage(x, y, 1, ship);

			return;
		}
		if (nair > MIN_AIR_FOR_FIRE){
			//nBlock |= (1 << Ship.BLOCK_FIRE_BITS);
			//map2.onFire.put(x + y * map2.width, 1);
		} else {
			map2.onFire.remove(x + y * map2.width, 0);
			nBlock &= ~((1 << Ship.BLOCK_FIRE_BITS));
		}
		nBlock |= (nair << Ship.BLOCK_AIR_BITS);
		map2.set(x, y, nBlock);
	}
	public Color getColor(int block, int x, int y, Ship ship) {
		//if (block != 0) Gdx.app.log(TAG, "get color " + block);
		int damage = ((block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS);
		int id = (block & Ship.BLOCK_ID_MASK); 
		/*if ((block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS == 1){
			tmpC.set(CustomColors.mapDrawColors[(id) + 16]);
			return tmpC;
		}*/
		int boost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		if (boost > 0){
			tmpC.set(CustomColors.mapDrawColors[(id) + 32]);
			return tmpC;
		}
		
		if (id == Ship.FLOOR ){
			tmpC.set(CustomColors.mapDrawColors[(id)]);
			int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			float alpha = air / 16f;
			if (air < 5) alpha = 0f;
			//alpha *= 16;
			alpha = Math.min(Math.max(alpha, 0), 1);
			alpha = 1 - alpha;
			alpha *= .3f;
			tmpC.b = alpha;
			return tmpC;

		} 
		
		tmpC.set(CustomColors.mapDrawColors[id]);
		
		
		float depl = ((block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS) / (float)ship.maxDepletionBySystem[id];

		if (damage >= ship.damageThreshold[id]){
			
			tmpC.set(CustomColors.mapDrawColors[id+48]);
			tmpC.a = 0f;
			//tmpC.g = 1f - damage/2f;
			//Gdx.app.log(TAG, "damage" + damage);
		} else {
			
			tmpC.g = 1f - depl/2f;
		}
		
		//if (depl > .01f && depl < 0.9f)Gdx.app.log(TAG, "depletion" + depl);
		return tmpC;
	}

	public void updateBlocks(Ship ship){
		int x = updateOrder[updateProgress*2];
		int y = updateOrder[updateProgress*2+1];
		updateProgress++;
		updateProgress %= updateOrderLength;
		for (int cx = 0; cx <= chunksX; cx++)
			for (int cy = 0; cy <= chunksY; cy++){
				int dx = cx * Ship.CHUNKSIZE + x, dy = cy * Ship.CHUNKSIZE + y;
				if (dx <= 0 || dy <= 0 || dx >= width-1 || dy >= height-1){
					continue;
				}
				int block = get(dx, dy);
				if (block != 0){
					//Gdx.app.log(TAG, "block upd" + (block & Ship.BLOCK_ID_MASK) + " dx " + dx + ", " + dy);
				}
				BlockDef def = defs[block & Ship.BLOCK_ID_MASK];
				def.update(dx, dy, block, this, ship);
				//updateAir(x, y, block, ship);
			}
		
		updateSystemBlocks(ship);
	}
	private void updateSystemBlocks(Ship ship) {
		
	}
	private final void updateAir(int x, int y, int block, Ship ship) {
		//if (true) return;
		int air = block & Ship.BLOCK_AIR_MASK;
		air >>= Ship.BLOCK_AIR_BITS;
			int dx = 0, dy = 0;
			if (MathUtils.randomBoolean()) dx = MathUtils.randomBoolean()?-1:1;
			else dy = MathUtils.randomBoolean()?-1:1;
			//Gdx.app.log(TAG, "updatesdfskdjl");
			int otherb = get(x+dx, y+dy);
			int otherID = otherb & Ship.BLOCK_ID_MASK;
			int selfFire = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
		if (selfFire == 0){
			if ((otherID) == Ship.WALL && 
					((otherb & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS) == 0){
				return;
			} else if (otherID == Ship.VACCUUM){
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);//
				//Gdx.app.log(TAG, "air di");
				int self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				//self |= (2 << Ship.BLOCK_FIRE_BITS);
				set(x, y, self);
				onFire.remove(x + y * width, 0);
				return;
			}
			
			int otherAir = otherb & Ship.BLOCK_AIR_MASK;
			otherAir >>= Ship.BLOCK_AIR_BITS;
			if (air >= otherAir){
				int diff = (air - otherAir);
				if (diff % 2 == 0) {
					diff /= 2;
				} else {
					diff /= 2;
					//diff += 1;
				}
				int add = MathUtils.random(4);
				if (otherAir > 4)
					diff += add;
				diff = Math.min(diff,  air-add);
				//diff = 1;
				int self = air - diff - (MathUtils.random(10000000) == 0?1:0);
				self = Math.max(0,  self);
				self  = Math.min(63,  self);
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				set(x, y, self);
				onFire.remove(x + y * width, 0);
	
				otherAir += diff;
				otherAir = Math.min(63,  otherAir);
				otherAir  = Math.max(0,  otherAir);
				otherAir <<= Ship.BLOCK_AIR_BITS;
				otherAir &= Ship.BLOCK_AIR_MASK;
				otherAir |= (otherb & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				set(x+dx, y+dy, otherAir);
				onFire.remove(x + dx + (y + dy) * width, 0);

			}
		} else if (selfFire == 1){
			int otherFire = (otherb & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
			if ((otherID) == Ship.WALL && 
					((otherb & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS) == 0){
				int self = air - 1;
				self = Math.max(0,  self);
				self  = Math.min(63,  self);
				if (self < FIRE_THRESHOLD){
					self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
					set(x, y, self);
					onFire.remove(x + y * width, 0);
					//Gdx.app.log(TAG, "reset air");
					return;
				}
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (1 << Ship.BLOCK_FIRE_BITS);
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				set(x, y, self);
				onFire.put(x + y * width, 0);
				return;
			} else if (otherID == Ship.VACCUUM){
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);
				int self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				//self |= (2 << Ship.BLOCK_FIRE_BITS);

				set(x, y, self);
				onFire.remove(x + y * width, 0);
				return;
			}

			if (otherFire == 0){
				//otherAir += diff;
				int self = air - 1;
				if (self < FIRE_THRESHOLD){
					self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
					set(x, y, self);
					onFire.remove(x + y * width, 0);
					//Gdx.app.log(TAG, "reset air SPREAD");
					return;
				}
				int otherAir = otherb & Ship.BLOCK_AIR_MASK;
				otherAir >>= Ship.BLOCK_AIR_BITS;
				otherAir = Math.min(63,  otherAir);
				otherAir  = Math.max(0,  otherAir);
				otherAir <<= Ship.BLOCK_AIR_BITS;
				otherAir &= Ship.BLOCK_AIR_MASK;
				otherAir |= (otherb & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				otherAir |= (1 << Ship.BLOCK_FIRE_BITS);

				set(x+dx, y+dy, otherAir);
				onFire.put(x + dx + (y + dy) * width, 0);
				damage(x+dx, y+dy, 1, ship);
				//Gdx.app.log(TAG, "damage");
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (1 << Ship.BLOCK_FIRE_BITS);
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				set(x, y, self);
				onFire.put(x + y * width, 0);


			} else if (otherFire == 1){

				int self = air - 2;
				self = Math.max(0,  self);
				self  = Math.min(63,  self);
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);
				if (self < FIRE_THRESHOLD){
					self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
					set(x, y, self);
					onFire.remove(x + y * width, 0);
					//Gdx.app.log(TAG, "reset air");
					return;
				}
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (1 << Ship.BLOCK_FIRE_BITS);
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));

				set(x, y, self);
				onFire.put(x + y * width, 0);
			}

		}//selffire == 1
		else if (selfFire == 2) for (int k = 0; k < 1; k++){}


	}
	
	public void setRect(int x, int y, int block, int s, Ship parent) {
		//int s = (int) e();
		int h = s/2;
		for (int bx = x - h; bx < x -h+s; bx++)
			for (int by = y - h; by < y -h+s; by++){
				set(bx, by, block);
				parent.setDirty(bx, by);
			}
	}
	public void overWriteFrom(IntPixelMap m) {
		for (int i = 0; i < m.map.length; i++){
			if (m.map[i] != 0) map[i] = m.map[i];
		}

	}
	public void clear() {
		for (int i = 0; i < map.length; i++){
			map[i] = 0;
		}
		cleared = true;
	}
	public void clear(int replacement) {
		for (int i = 0; i < map.length; i++){
			map[i] = replacement;
		}		
	}
	public int[] getRawBlocks() {
		
		return map;
	}
	public void setRawBlocks(int[] blocks) {
		
		map = blocks;
		
	}
	public void updateAirNew(IntPixelMap fill, Ship ship) {
		for (int i = 0; i < 2; i++) {
			int x = MathUtils.random(1, width-2);
			int y = MathUtils.random(1, height-2);
			int block = get(x, y);
			int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			if (air > 10)
				fill.resetRandomFloodFill(200, 250);
			else 
				fill.resetRandomFloodFill(600, 850);
			//block = MathUtils.random(1, 3);
			fill.randomFloodFill(this, x, y
					, airUpdateReplaceIndex, MathUtils.random(20000, 100000), false, ship);
			
			}
		for (int i = 0; i < 2; i++){
			int x = MathUtils.random(1, width-2);
			int y = MathUtils.random(1, height-2);
			int block = get(x, y);
			int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			
			fill.resetRandomFloodFill(150, 200);
			
			//block = MathUtils.random(1, 3);
			fill.randomFloodFill(this, x, y
					, airUpdateReplaceIndex, MathUtils.random(20000, 100000), true, ship);
		}
		if (ship.systemBlocks[Ship.OXYGEN] != null && ship.systemBlocks[Ship.OXYGEN].size > 0)
		{
			int ind = MathUtils.random(ship.systemBlocks[Ship.OXYGEN].size-1);
			
			int x = ship.systemBlocks[Ship.OXYGEN].get(ind).x;
			int y = ship.systemBlocks[Ship.OXYGEN].get(ind).y;
			int block = get(x, y);
			int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
			//if (air > 10)
				fill.resetRandomFloodFill(100, 150);
			//else 
			//	fill.resetRandomFloodFill(600, 850);
			//block = MathUtils.random(1, 3);
			fill.randomFloodFill(this, x, y
					, airUpdateReplaceIndex, MathUtils.random(20000, 100000), false, ship);
			//updateAir(x, y, block, ship);
			
		}
		airUpdateReplaceIndex++;
		if (airUpdateReplaceIndex > 10) airUpdateReplaceIndex = 1;
	}

}