package ninja.trek;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pools;

public class IntPixelMap{
	private static final String TAG = "int pixel map";
	private static final int FIRE_THRESHOLD = 1;
	private static int[] updateOrder;
	private static int updateOrderLength;
	private static BlockDef[] defs;
	int[] map;
	public int width, height, chunksX, chunksY;
	int chunkSize;
	private boolean specialConstructor;
	private int updateProgress;
	private int updateRepeats;
	public GridPoint2 spawn = new GridPoint2();
	public IntIntMap[]
					//,
					damaged = new IntIntMap[Ship.systemNames.length];
	public IntIntMap onFire = new IntIntMap();
	//public Bits onFire = new Bits();
	public IntIntMap[] needsBoost = new IntIntMap[Ship.systemNames.length];
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
				
				int neededAir = 511 - air;
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
	public IntPixelMap(int w, int h, int chunkSize){
		width = w;
		height = h;
		this.chunkSize = chunkSize;
		chunksX = width / chunkSize + 1;
		chunksY = height / chunkSize + 1;
		map = new int[chunksX * chunkSize * chunksY * chunkSize];

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
		this(map.width, map.height, map.chunkSize);
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
		int blockIndex = (x) + (y) * chunksX * chunkSize;
		
		
		

		return map[blockIndex];
	}
	public void set(int x, int y, int b) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//int currentBoost = (map[x  + y * chunkSize * chunksX] & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		//if (currentBoost == 1) throw new GdxRuntimeException("Edkl)" + (b & Ship.BLOCK_BOOST_MASK));
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		map[x  + y * chunkSize * chunksX] = b;

	}
	public void damage(int x, int y, int dam, Ship ship) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		int index = x  + y * chunkSize * chunksX;
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
	
	
	private void markDamagedIndex(int index, int id) {
		damaged[id].put(index, 0);
	}
	
	private void unmarkDamaged(int index, int id) {
		damaged[id].remove(index, 0);
		
	}
	public void fix(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		int index = x  + y * chunkSize * chunksX;
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
	public void fightFire(int x, int y) {
		if (x >= width || y >= height || x < 0 || y < 0) return ;
		//if (specialConstructor)Gdx.app.log(TAG, "set " + x + ", " + y);
		int index = x  + y * chunkSize * chunksX;
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
		int index = x  + y * chunkSize * chunksX;
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
	public void boostAll() {
		//Gdx.app.log(TAG, "BOOST ALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				boost(x, y);
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
		if (id == Ship.WALL || id == Ship.VACCUUM) return;
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
	public Color getColor(int block, int x, int y, Ship ship) {
		//if (block != 0) Gdx.app.log(TAG, "get color " + block);
		int damage = ((block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS);
		int id = (block & Ship.BLOCK_ID_MASK); 
		int boost = (block & Ship.BLOCK_BOOST_MASK) >> Ship.BLOCK_BOOST_BITS;
		if (boost > 0){
			tmpC.set(CustomColors.mapDrawColors[(id) + 96]);
			return tmpC;
		}
		if ((block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS == 1){
			//if (id != Ship.FLOOR){
				if (damage >=
				ship.damageThreshold[id]
						){
					int mod = (x+y )%2;
					//mod = 0;
					if (mod == 0)
						tmpC.set(CustomColors.mapDrawColors[(id) + 64]);
					else 
						tmpC.set(CustomColors.mapDrawColors[(id) + 80]);
					
				} else 
					tmpC.set(CustomColors.mapDrawColors[(id) + 16]);
				return tmpC;
				
			//}
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
			int mod = (x+y )%2;
			mod = 0;
			tmpC.set(CustomColors.mapDrawColors[id+32 + 16*mod]);
			
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
				int dx = cx * chunkSize + x, dy = cy * chunkSize + y;
				if (dx <= 0 || dy <= 0 || dx >= width-1 || dy >= height-1){
					continue;
				}
				int block = get(dx, dy);
				if (block != 0){
					//Gdx.app.log(TAG, "block upd" + (block & Ship.BLOCK_ID_MASK) + " dx " + dx + ", " + dy);
				}
				BlockDef def = defs[block & Ship.BLOCK_ID_MASK];
				def.update(dx, dy, block, this, ship);
				updateAir(dx, dy, block, ship);
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
					ship.setDirty(x,  y);
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
				ship.setDirty(x,  y);
				return;
			} else if (otherID == Ship.VACCUUM){
				//Gdx.app.log(TAG, "air diff " + diff + " air " + self + "  other " + other  + " block " + block);
				int self = (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				//self |= (2 << Ship.BLOCK_FIRE_BITS);

				set(x, y, self);
				onFire.remove(x + y * width, 0);
				ship.setDirty(x,  y);
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
					ship.setDirty(x,  y);
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
				ship.setDirty(x+dx,  y+dy);
				damage(x+dx, y+dy, 1, ship);
				//Gdx.app.log(TAG, "damage");
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (1 << Ship.BLOCK_FIRE_BITS);
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));
				set(x, y, self);
				onFire.put(x + y * width, 0);
				ship.setDirty(x,  y);


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
					ship.setDirty(x,  y);
					return;
				}
				self <<= Ship.BLOCK_AIR_BITS;
				self &= Ship.BLOCK_AIR_MASK;
				self |= (1 << Ship.BLOCK_FIRE_BITS);
				self |= (block & (Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_DATA_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK));

				set(x, y, self);
				onFire.put(x + y * width, 0);
				ship.setDirty(x,  y);
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
	}
	public void clear(int replacement) {
		for (int i = 0; i < map.length; i++){
			map[i] = replacement;
		}		
	}
	
}