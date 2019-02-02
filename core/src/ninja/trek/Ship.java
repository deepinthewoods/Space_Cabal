package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ScreenUtils;

import ninja.trek.ui.ItemDisplay;
import ninja.trek.ui.ItemDisplay.ItemButton;
import ninja.trek.ui.UIActionButton;
import ninja.trek.ui.UISystemButton;

public class Ship {
	private static final String TAG = "Ship";
	public static final int BLOCK_ID_MASK = 0x007f
			, BLOCK_BOOST_BITS = 7, BLOCK_BOOST_MASK = 0x01 << BLOCK_BOOST_BITS
			, BLOCK_DAMAGE_BITS = 8, BLOCK_DAMAGE_MASK = 0x0f << BLOCK_DAMAGE_BITS
			, BLOCK_AIR_BITS = 12, BLOCK_AIR_MASK = 0x1ff << BLOCK_AIR_BITS
			, BLOCK_FIRE_BITS = 21, BLOCK_FIRE_MASK = 0x03 << BLOCK_FIRE_BITS
			, BLOCK_DATA_BITS = 23, BLOCK_DATA_MASK = 0x3f << BLOCK_DATA_BITS
			, BLOCK_EXTRA_BITS = 30, BLOCK_EXTRA_MASK = 0x3 << BLOCK_EXTRA_BITS;

	public static final int MAX_DAMAGE = 15
			, MAX_FIRE_SPRITES = 170;
	private final Sprite[] chunkSprites;
	public boolean placeDoor;
	public boolean deleteDoor;
	private GridPoint2[] roomBlocks = new GridPoint2[1000];
	private int maxSysRoomID;
	private float fireTime;
	private IntIntMap fireBlockIndices = new IntIntMap();
	public boolean hasCalculatedConnectivity;
	private int maxRoomID;

	public void openDoor(Door door) {
		int s = door.radius, h = s/2;
		int x = door.x;
		int y = door.y;
		for (int bx = x - h; bx < x -h+s; bx++)
			for (int by = y - h; by < y -h+s; by++){

			int block = map.get(bx, by);
			int id = block & Ship.BLOCK_ID_MASK;
			BlockDef def = IntPixelMap.defs[id];
			if (id == DOOR){
				id = DOOR;
				//block &= ~BLOCK_ID_MASK;
				//block |= id;
				block &= ~BLOCK_BOOST_MASK;
				block |= 1 << BLOCK_EXTRA_BITS;

				map.set(bx, by, block);
			}
		}
		hasCalculatedConnectivity = false;//TODO maybe precompute
		Gdx.app.log(TAG, "open door " +x+"," +y);
	}

	public void closeDoor(Door door) {
		int s = door.radius, h = s/2;
		int x = door.x;
		int y = door.y;
		for (int bx = x - h; bx < x -h+s; bx++)
			for (int by = y - h; by < y -h+s; by++){

				int block = map.get(bx, by);
				int id = block & Ship.BLOCK_ID_MASK;
				BlockDef def = IntPixelMap.defs[id];
				if (id == DOOR){
					//id = DOOR;
					//block &= ~BLOCK_ID_MASK;
					//block |= id;
					block &= ~BLOCK_BOOST_MASK;
					block &= ~BLOCK_EXTRA_MASK;

					map.set(bx, by, block);
				}
			}
		hasCalculatedConnectivity = false;
	}

    public void missileDamage(int x, int y, int damage) {
        map.floodFillMissileDamage(x, y, damage, this);

    }

    public void damageShield(int shieldDamage) {
        ShipEntity s = getShipEntity();
        s.shield = Math.max(0, s.shield - shieldDamage);
    }

    public void laserDamage(int x, int y, int damage) {
        map.floodFillMissileDamage(x, y, damage, this);
       // Gdx.app.log(TAG, "LASER DAMAGE");
    }


    public enum Alignment {CENTRE, TOP_RIGHT};
	public Alignment alignment = Alignment.CENTRE;
	public int[] systemButtonOrder = new int[systemNames.length];
	public int[] maxDepletionBySystem = {63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63};
	public float[] maxDamgeBySystem =   {15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15};;
	public boolean placeWeapon = false;
	public boolean deleteWeapon;
	public boolean placeSpawn;
	private int cacheProgress;
	private Array<GridPoint2> chunksInRandomOrder = new Array<GridPoint2>();
	private Array<GridPoint2> chunksInRandomOrderForCaching = new Array<GridPoint2>();
	public int tick;
	public static String[] systemNames = {"Vac", "Engine", "Weapon", "Shield", "Wall", "Floor", "Oxygen", "Drone", "Teleporter", "Science", "Door"};
	public static final int VACCUUM = 0; 
	public static final int ENGINE = 1;
	public static final int WEAPON = 2;
	public static final int SHIELD = 3;
	public static final int WALL = 4;
	public static final int FLOOR = 5;
	public static final int OXYGEN = 6;
	public static final int DRONE = 7;
	public static final int TELEPORTER = 8;
	public static final int SCIENCE = 9;
	public static final int DOOR = 10;
	protected int[] damageThreshold = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	public static final int CHUNKSIZE = 64;
	public int mapWidth;
	public int mapHeight;
	public int chunksX;
	public int chunksY;
	private final int cacheIterations = 2;
	private static final int MAP_EXTRA_PIXELS = 64;
	static final float GAP = 10;
	private static final int SHIELD_SPACING = 12;
	private static final int MAX_WIDTH = 512;
	private static final int MAX_HEIGHT = 512;
	private static final float ZOOM_SPEED = 5f;
	public static final int MAX_ENTITIES = 10;
	private transient FrameBuffer[] chunkBuffer, fillBuffer, wireBuffer;
	private transient Texture[] chunkTextures, fillTextures, wireTextures;;

	private transient boolean[] dirtyChunk;
	private transient TextureRegion pixel;
	private float backR = .01f//.31f
			, backG = .01f//.31f
			, backB = .01f;//0.1872974f;
	public IntPixelMap map, fill, wire;
	public IntPixelMap room;
	private transient Bits drawn = new Bits(), hasBuffer = new Bits(), toFree = new Bits();
	public transient Pool<FrameBuffer> bufferPool;
	transient Vector3 vec = new Vector3();
	public Vector2  offset = new Vector2();
	private int pixelSize;
	private EntityArray entities = new EntityArray();
	private transient FontManager fonts;
	public float zoom = 1f;
	public transient final AStar2 aStar;
	private transient Vector3 v = new Vector3(), v2 = new Vector3();
	public transient OrthographicCamera camera = new OrthographicCamera();
	public transient boolean drawFill = true;
	public transient boolean drawWires = false;
	public transient boolean editMode = false;
	private transient ShaderProgram shader;
	public AStarDeplete depleter;
	public OuterHull hull = new OuterHull();
	public Array<GridPoint2>[] systemBlocks = new Array[16];
	public boolean hasCategorizedBlocks =false;
	Vector2 q = new Vector2(), r = new Vector2();
	
	public IntArray inventory = new IntArray(true, 8);
	public IntPixelMap systemRooms;
	private IntArray[] roomsBySystem;

	public boolean[] disabledButton = new boolean[systemNames.length];
	public boolean hullFront;
	public Array<GridPoint2> roomCentres = new Array<GridPoint2>();
	public float[][] cacheVerts;
	private int[] cacheDrawCount;
	private float maxZoomForCentering;
	private float zoomedOutEnemyZoom;
	private float zoomInTarget;
	private float zoomAlpha;
	GridPoint2 point = new GridPoint2();
	protected boolean zoomingOut;
	private Entity selectedEntity;
	public boolean zoomingIn;
	private BloomN bloom;
	public float[][] cacheVertsFill;
	
	public Ship(IntPixelMap map, Sprite pixelSprite, FontManager fonts, ShaderProgram shader){
		if (pixelSprite.getHeight() != pixelSprite.getWidth()) throw new GdxRuntimeException(" prites not square");
		pixelSize = (int) pixelSprite.getHeight();
		
		this.mapWidth = map.width;
		this.mapHeight = map.height;
		shieldRadius = Math.max(mapWidth/2,  mapHeight/2);
		shieldRadius *= 1.5f;
		shieldRadius2 = shieldRadius * shieldRadius;
		chunksX = mapWidth /CHUNKSIZE + (mapWidth %CHUNKSIZE == 0?0:1);
		chunksY = mapHeight /CHUNKSIZE + (mapHeight %CHUNKSIZE == 0?0:1);
		this.fonts = fonts;
		this.map = map;
		this.shader = shader;
		wire = new IntPixelMap(map);
		fill = new IntPixelMap(map);
		room = new IntPixelMap(map);
		systemRooms = new IntPixelMap(map);
		roomsBySystem = new IntArray[systemNames.length];
		for (int i = 0; i < roomsBySystem.length; i++){
			roomsBySystem[i] = new IntArray();
		}
		//Gdx.app.log(TAG, "map width " + chunksX + "  ,  " + chunksY + "  chunksize " + CHUNKSIZE + "  w " + mapWidth);
		chunkBuffer = new FrameBuffer[8 * 8];
		chunkTextures = new Texture[8 * 8];
		chunkSprites = new Sprite[8 * 8];
		fillBuffer = new FrameBuffer[8 * 8];
		fillTextures = new Texture[8 * 8];
		wireBuffer = new FrameBuffer[8 * 8];
		wireTextures = new Texture[8 * 8];
		dirtyChunk = new boolean[8 * 8];
		for (int i = 0; i < dirtyChunk.length; i++){
			dirtyChunk[i] = true;
		}
		
		pixel = pixelSprite;
		bufferPool = new Pool<FrameBuffer>(){

			@Override
			protected FrameBuffer newObject() {
				FrameBuffer buff = new FrameBuffer(Format.RGB888, CHUNKSIZE * pixelSize, CHUNKSIZE * pixelSize, false);
				return buff;
			}
			
		};
		aStar = new AStar2(MAX_WIDTH, MAX_HEIGHT, this);
		depleter = new AStarDeplete(mapWidth, mapHeight, this);

		for (int i = 0; i < systemButtonOrder.length; i++){
			systemButtonOrder[i] = i;
		}
		populateRandomChunkOrder();

		cacheVerts = new float[(MAX_WIDTH / CHUNKSIZE) * (MAX_HEIGHT / CHUNKSIZE)][CHUNKSIZE * CHUNKSIZE * 3 * 4];
		cacheVertsFill = new float[(MAX_WIDTH / CHUNKSIZE) * (MAX_HEIGHT / CHUNKSIZE)][CHUNKSIZE * CHUNKSIZE * 3 * 4];
		cacheDrawCount = new int[(MAX_WIDTH / CHUNKSIZE) * (MAX_HEIGHT / CHUNKSIZE)];
		
		maxZoomForCentering =  (float)mapHeight / (float)Gdx.graphics.getHeight();
		//Gdx.app.log(TAG, "max zoom" + maxZoomForCentering + "  h " + mapHeight);
		zoomedOutEnemyZoom = 5 * (float)mapHeight / (float)Gdx.graphics.getHeight();
		zoomInTarget = maxZoomForCentering * 1.5f;
		zoomAlpha = 0f;
		maxZoomForCentering *= 2f;
		bloom = new BloomN();
		bloom.setTreshold(.8f);
		
	}

	public FrameBuffer makeFrameBuffer(int i){
		int ind = hasBuffer.nextSetBit(0), count = 0;
		while (ind != -1){
			count++;
			ind = hasBuffer.nextSetBit(ind+1);
		}
		//Gdx.app.log(TAG, "create buffer" + i + "   / " + count);
		FrameBuffer buff = bufferPool.obtain();
		chunkBuffer[i] = buff;
		chunkTextures[i] = chunkBuffer[i].getColorBufferTexture();
		chunkTextures[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		chunkSprites[i] = new Sprite(chunkTextures[i]);
		buff = bufferPool.obtain();
		fillBuffer[i] = buff;
		fillTextures[i] = fillBuffer[i].getColorBufferTexture();
		fillTextures[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		buff = bufferPool.obtain();
		wireBuffer[i] = buff;
		wireTextures[i] = wireBuffer[i].getColorBufferTexture();
		wireTextures[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		hasBuffer.set(i);
		dirtyChunk[i] = true;
		return buff;
	}
	
	public void set(IntPixelMap map){
		this.map = map;
	}
	public void setEntities(EntityArray ent){
		entities = ent;
	}
	private void clearFrameBuffer(int i) {
		if (chunkBuffer[i] == null) throw new GdxRuntimeException("Null Buffer: cannot be freed");
		int ind = hasBuffer.nextSetBit(0), count = 0;
		while (ind != -1){
			count++;
			ind = hasBuffer.nextSetBit(ind+1);
		}
		//Gdx.app.log(TAG, "clear  buffer" + i + "   / " + count);
		bufferPool.free(chunkBuffer[i]);
		chunkBuffer[i] = null;
		chunkTextures[i] = null;
		chunkSprites[i] = null;
		bufferPool.free(fillBuffer[i]);
		fillBuffer[i] = null;
		fillTextures[i] = null;
		bufferPool.free(wireBuffer[i]);
		wireBuffer[i] = null;
		wireTextures[i] = null;
		hasBuffer.clear(i);
		
		ind = hasBuffer.nextSetBit(0); count = 0;
		while (ind != -1){
			count++;
			ind = hasBuffer.nextSetBit(ind+1);
		}
		//Gdx.app.log(TAG, "clear  buffer post" + i + "   / "  + count);
	}
	
	

	public void cacheChunk(IntPixelMap map, float[][] cacheVerts) {
		if (map == fill) Gdx.app.log(TAG,  "draw fill");;
		GridPoint2 pt;
		//if dirty do that chunk first else 
		{
			cacheProgress++;
			if (cacheProgress >= chunksInRandomOrderForCaching.size) cacheProgress = 0;
			pt = chunksInRandomOrderForCaching.get(cacheProgress);
		}
		int x = pt.x, y = pt.y;
		//Gdx.app.log(TAG, "cache chunk" + x + "," + y);
		int chunkIndex = x + y * chunksX;
		float[] verts = cacheVerts[chunkIndex]; 
		int i = 0;
		for (int xx = 0; xx < CHUNKSIZE; xx++)
			for (int yy = 0; yy < CHUNKSIZE; yy++){
				//int blockIndex = (x*CHUNKSIZE + xx) + (y * CHUNKSIZE + yy) * chunksX * CHUNKSIZE;
				//Gdx.app.log(TAG, "cache block" + blockIndex + "," + x + "," + xx + "," + y + "," + yy + "," );
				int ax = x*CHUNKSIZE + xx , ay =  y * CHUNKSIZE + yy;
				int block = map.get(ax, ay);
				float c = map.getColor(block, ax, ay, this).toFloatBits();
				verts[i++] = xx;
				verts[i++] = CHUNKSIZE - yy-1;
				verts[i++] = c;
				verts[i++] = xx+1;
				verts[i++] = CHUNKSIZE - yy-1;
				verts[i++] = c;
				verts[i++] = xx;
				verts[i++] = CHUNKSIZE - yy-1+1;
				verts[i++] = c;
				verts[i++] = xx+1;
				verts[i++] = CHUNKSIZE - yy-1+1;
				verts[i++] = c;
				
				
				//batch.setColor(map.getColor(block, ax, ay, this));
				//batch.draw(pixel, xx, CHUNKSIZE - yy-1, 1, 1);
			}
		cacheDrawCount[chunkIndex] = CHUNKSIZE * CHUNKSIZE;
	}
	Matrix4 proj = new Matrix4();
	private int renderProgress;
	private boolean redrawFill;
	private boolean redrawMap;
	public void drawCachedChunk(int x, int y, FrameBuffer[] buffer, IntPixelMap map, Mesh mesh, ShaderProgram cacheShader, float[][] cacheVerts) {
		//if (map == fill) Gdx.app.log(TAG,  "draw fill");;
		//Gdx.app.log(TAG, "cache chunk" + x + "," + y);
		int chunkIndex = x + y * chunksX;
		//int[] chunk = chunkData[chunkIndex];
		if (buffer[chunkIndex] == null)
			makeFrameBuffer(chunkIndex);
		proj.setToOrtho2D(0, 0, CHUNKSIZE, CHUNKSIZE);
		
		cacheShader.begin();
		cacheShader.setUniformMatrix("u_projTrans", proj);
		buffer[chunkIndex].begin();
		Gdx.gl.glClearColor(backR, backG, backB , 1f);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		mesh.setVertices(cacheVerts[chunkIndex]);
		mesh.render(cacheShader, GL20.GL_TRIANGLES, 0, cacheDrawCount[chunkIndex] * 6);
		
		buffer[chunkIndex].end();
		cacheShader.end();
	}

	
	public void updateDraw( Mesh mesh, ShaderProgram cacheShader){
		//update(chunkBuffer, batch);
		if (map.getRawBlocks() == null) return;
		
		
		for (int i = 0; i < cacheIterations; i++){
			
			renderProgress++;
			
			if (renderProgress >= chunksInRandomOrder.size) renderProgress = 0;
			
			GridPoint2 pt = chunksInRandomOrder.get(renderProgress);
			//cacheChunk(pt.x, pt.y, map);
			//if (editMode)cacheChunk(pt.x, pt.y, batch, fillBuffer, fill);
			drawCachedChunk(pt.x, pt.y, chunkBuffer, map, mesh, cacheShader, cacheVerts);
			
		}
	}
	/** 
	 * @param x world x coord
	 * @param y world y coord
	 */
	public void setDirty(int x, int y){
		if (x >= mapWidth || y >= mapHeight || x < 0 || y < 0) return;
		int chunkX = x / CHUNKSIZE, chunkY = y / CHUNKSIZE, chunkIndex = chunkX + chunkY * chunksX;
		dirtyChunk[chunkIndex] = true;
	}
	
	public void setRedrawFill() {
		redrawFill = true;
	}
	
	public void setRedrawMap() {
		redrawMap = true;
	}
	
	public void setAllDirty() {
		for (int i = 0; i < dirtyChunk.length; i++)
			dirtyChunk[i] = true;
	}
	/** Sets all chunks in a rectangle to dirty
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void setDirty(int x0, int y0, int x1, int y1){
		int chunkX0 = x0 / CHUNKSIZE, chunkY0 = y0 / CHUNKSIZE;
		int chunkX1 = x1 / CHUNKSIZE, chunkY1 = y1 / CHUNKSIZE;
		for (int chunkX = chunkX0; chunkX <= chunkX1; chunkX++)
			for (int chunkY = chunkY0; chunkY <= chunkY1; chunkY++){
				int chunkIndex = chunkX + chunkY * chunksX;
				dirtyChunk[chunkIndex] = true;
			}
	}
	Color col = new Color(Color.WHITE);
	public void draw(SpriteBatch batch, OrthographicCamera wcamera, World world, boolean paused, Texture indexColors, Mesh mesh, ShaderProgram cacheShader, boolean overrideHullFront){
		//batch.getProjectionMatrix().set(camera.combined);
		//Gdx.app.log(TAG, "draw map");
		//if (!paused)
		    stateTime += Gdx.graphics.getDeltaTime();
		
		if (editMode && redrawFill) {
			redrawFill = false;
			int x0, x1, y0, y1;
			x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
			for (int x = x0; x <= x1; x++)
				for (int y = y0; y <= y1; y++){
					cacheChunk(fill, cacheVertsFill);
					drawCachedChunk(x, y, fillBuffer, fill, mesh, cacheShader, cacheVertsFill);
				}
		}
		
		if (redrawMap) {
			Gdx.app.log(TAG, "redraw map");
			redrawMap = false;
			int x0, x1, y0, y1;
			x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
			for (int x = x0; x <= x1; x++)
				for (int y = y0; y <= y1; y++){
					cacheChunk(map, cacheVerts);
					drawCachedChunk(x, y, chunkBuffer, map, mesh, cacheShader, cacheVerts);
				}
		}
		batch.setProjectionMatrix(camera.combined);
		if (!hullFront && showHull){
			//batch.enableBlending();
			hull.draw(batch, wcamera, world, this);
			
		}
		batch.setShader(shader);
		shader.begin();
		shader.setUniformi("u_index_texture", 1); //passing first texture!!!
		shader.setUniformf("u_time", stateTime);
		//Colors.bind(0);
		
		//shader.setUniformi("u_texture", 0); //passing first texture!!!
		//batch.setShader(null);
		shader.end();
		drawn.clear();
		batch.enableBlending();
		//col.set(Color.WHITE);
		//col.a = .5f;
		batch.setColor(Color.WHITE);

		
		batch.begin();
		
		//float[] colorArray = CustomColors.getFloatColorArray();
		//shader.setUniform3fv("u_colors[0]", colorArray , 0, colorArray.length);
		//bloom.capture();
		int x0, x1, y0, y1;
		x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
				//Gdx.app.log(TAG, "draw map");
				drawChunk(x, y, batch, drawWires, drawFill, indexColors);
				drawn.set(x + y * chunksX);
			}
		//batch.draw(img, 0, 0);
		batch.end();
		//bloom.render();
		toFree.clear();
		toFree.or(hasBuffer);
		toFree.andNot(drawn);
		
		int nextFreeIndex = toFree.nextSetBit(0);
		while (nextFreeIndex != -1) {
			clearFrameBuffer(nextFreeIndex);
			nextFreeIndex = toFree.nextSetBit(nextFreeIndex+1);
		}
		//Gdx.app.log(TAG, "onfire " + map.onFire.size);
		batch.setProjectionMatrix(camera.combined);
		batch.setColor(Color.WHITE);
		batch.setShader(null);
		batch.begin();
		IntIntMap.Entries fireIterator = fireBlockIndices.entries();
		fireTime += Gdx.graphics.getDeltaTime();
		while (fireIterator.hasNext()){

			int val = fireIterator.next().key;
			int x = val % map.width;
			int y = val / map.width;

			TextureRegion fireS = Sprites.fire.getKeyFrame(fireTime);
			batch.draw(fireS, x-8, y-3);
			//Gdx.app.log(TAG, "fire" + x + ", " + y);
		}

        int maxSprites = Math.min(MAX_FIRE_SPRITES, map.onFire.size / 32);
        maxSprites = Math.max(maxSprites, 8);
		if (fireTime > 1f / maxSprites && fireBlockIndices.size > 0){
			fireIterator = fireBlockIndices.entries();
			int c = 0;

			int index = 1;
			if (fireBlockIndices.size > 1)
				index = MathUtils.random(1, fireBlockIndices.size-1);

			while (c++ < index && fireIterator.hasNext())
				fireIterator.next();

			fireIterator.remove();
			fireTime = 0f;

		}

		batch.end();

		IntIntMap.Entries onFireIter = map.onFire.entries();

		int c = 0;
		if (map.onFire.size > 0){

			int index = MathUtils.random(0, map.onFire.size-1);
			for (int i = 0; i < index; i++){
				onFireIter.next();
			}
			while (onFireIter.hasNext() && fireBlockIndices.size < maxSprites
					){
				int key = onFireIter.next().key;
				if (!fireBlockIndices.containsKey(key)){
					fireBlockIndices.put(key, 0);
				}
			}
			onFireIter = map.onFire.entries();
			while (onFireIter.hasNext() && fireBlockIndices.size < maxSprites
					&& c < index){
				int key = onFireIter.next().key;
				if (!fireBlockIndices.containsKey(key)){
					fireBlockIndices.put(key, 0);
				}
			}
		}


		if ((hullFront && showHull) || overrideHullFront){
			//batch.enableBlending();
			hull.draw(batch, wcamera, world, this);
			
		}
		
		//Gdx.gl.glEnable(GL20.GL_BLEND);
		//Gdx.gl.glDisable(GL20.GL_BLEND);

	}

	private void drawChunk(int x, int y, SpriteBatch batch, boolean wire, boolean fill, Texture indexColors) {		
		//Gdx.app.log(TAG, "draw chunk" + x + "," + y + "   " + x * CHUNK_SIZE+ "   " + y * CHUNK_SIZE+ "   " + CHUNK_SIZE+ "   " + CHUNK_SIZE);
		//batch.setColor(MathUtils.random());
		Texture texx = chunkTextures[x + y * chunksX];
		Sprite s = chunkSprites[x + y * chunksX];
		if (texx != null) {
			indexColors.bind(1);
			batch.getShader().setUniformi("u_index_texture", 1);
			texx.bind(0);
			batch.getShader().setUniformi("u_texture", 0);
			batch.setColor(1f, 1f, 1f, .25f);
			//batch.draw(texx, (float)x * CHUNKSIZE, y * CHUNKSIZE, CHUNKSIZE, CHUNKSIZE);
			s.setBounds((float)x * CHUNKSIZE, y * CHUNKSIZE, CHUNKSIZE, CHUNKSIZE);
			s.setAlpha(.15f);
			s.draw(batch);

			
		}
		if (wire){
			Texture tex = wireTextures[x + y * chunksX];
			if (tex != null) batch.draw(tex,  (float)x * CHUNKSIZE, y * CHUNKSIZE, CHUNKSIZE, CHUNKSIZE);
		}
		if (editMode){
			Texture tex = fillTextures[x + y * chunksX];
			if (tex != null){
				//Gdx.app.log(TAG, "draw fill");
				batch.draw(tex, (float)x * CHUNKSIZE, y * CHUNKSIZE, CHUNKSIZE, CHUNKSIZE);
			}
		}
		
	}
	public static void disableScissor(){
		
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		
	}
	
	public void enableScissor(World world){
		if (alignment == Alignment.CENTRE){
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			v.set(-GAP, 0, 0);
			Ship otherShip = world.getEnemyShip();
			otherShip.camera.project(v);
		
			int width = (int) v.x;
			Gdx.gl.glScissor(0, 0, width, Gdx.graphics.getHeight());
		} else {
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			v.set(-GAP, 0, 0);
			camera.project(v);
		
			int width = (int) v.x;
			Gdx.gl.glScissor(width, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}
	public void drawEntities(SpriteBatch batch, World world, boolean forceHullOver, boolean isPlayer){
		fonts.setZoom(camera);
		//camera.update();
		//Gdx.app.log(TAG, "draw entities " + entities.size + "  "  + camera.position);
		//batch.setProjectionMatrix(camera.combined);//.translate(offset.x, offset.y, 0);
		//batch.setShader(null);
		if (forceHullOver){
			return;
		}


		if (isPlayer)
            for (Entity e : entities){
                fonts.draw(e, batch, camera);
            }
		
		
		//batch.enableBlending();
		for (Entity e : entities){
			e.draw(batch, camera, world);
		}

		if (editMode) fonts.drawSpawn(map.spawn, batch);
		//batch.disableBlending();
		//batch.end();
		
	}
	

	public void drawLines(ShapeRenderer shape, UI ui, boolean isSettingTarget, OrthographicCamera wcamera, World world){
		shape.setProjectionMatrix(camera.combined);
		shape.setColor(.31f, .31f, .31f, 1f);
		shape.begin(ShapeType.Line);
		
		if (editMode){
			shape.line(0, 0, 0, mapHeight);
			shape.line(0, 0, mapWidth, 0);
			shape.line(0, mapHeight, mapWidth, mapHeight);
			shape.line(mapWidth, 0, mapWidth, mapHeight);
		}
		
		v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(v);
		v.x = (int)v.x;
		v.y = (int)v.y;
		shape.setColor(Color.WHITE);
		if (editMode && ui.editLineButton.isChecked() && ui.previewLine){
			int s = (int) ui.brushSizeSlider.getValue();
			int h = s/2;
			int dx =  (int) ui.previewA.x - (int) ui.previewB.x;
			int dy =  (int) ui.previewA.y - (int) ui.previewB.y;
			//dx = -dx;
			//dy = -dy;
			v.set(ui.previewB.x, ui.previewB.y, 0);
			if (Math.abs(dx) > Math.abs(dy)){
				dy = 0;
			} else {
				dx = 0;
			}
			int x0 =(int) (v.x)
					, y0 = (int) (v.y)
					, x1 = (int) (v.x+dx)
					, y1 = (int) (v.y+dy);
			if (x0 > x1){
				int t = x0;
				x0 = x1;
				x1 = t;
			}
			if (y0 > y1){
				int t = y0;
				y0 = y1;
				y1 = t;
			}
			x0 += -h;
			y0 += -h;
			x1 += -h+s;
			y1 += -h+s;
			shape.line(x0, y0, x0, y1);
			shape.line(x0, y0, x1, y0);
			shape.line(x1, y1, x0, y1);
			shape.line(x1, y1, x1, y0);
			if (ui.xMirrorBtn.isChecked()){
				
				x0 -= 1;
				x1 -= 1;
				
				shape.line(mapWidth - 1 -x0, y0, mapWidth - 1 -x0, y1);
				shape.line(mapWidth - 1 -x0, y0, mapWidth - 1 -x1, y0);
				shape.line(mapWidth - 1 -x1, y1, mapWidth - 1 -x0, y1);
				shape.line(mapWidth - 1 -x1, y1, mapWidth - 1 -x1, y0);
				
			}
		} else
		if (editMode && !ui.fillBtn.isChecked()){
			int s = (int) ui.brushSizeSlider.getValue();
			int h = s/2;
			shape.line(v.x-h, v.y-h, v.x-h, v.y-h+s);
			shape.line(v.x-h, v.y-h, v.x-h+s, v.y-h);
			shape.line(v.x-h+s, v.y-h+s, v.x-h, v.y-h+s);
			shape.line(v.x-h+s, v.y-h+s, v.x-h+s, v.y-h);
		} 
		
		if (alignment == Alignment.TOP_RIGHT){
			//v.set(0, 0, 0);
			//v2.set(mapWidth, mapHeight, 0);\
			if (isSettingTarget) 
				shape.line(-GAP-2,-10000,-GAP-2, 1000);
			shape.line(-GAP,-10000,-GAP, 1000);
			
			//shape.line(0,0,0, 1000);
		}
		
		if (selectedEntity != null){
			Entity e = selectedEntity;
			float w = 20 * camera.zoom, h = w;
			shape.rect(e.x-w , e.y-h , w*2, h*2);
			//Gdx.app.log(TAG, "SELECTED");
		}
		shape.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glLineWidth(8f);
		shape.begin(ShapeType.Line);
		shape.setColor(CustomColors.SHIELD);
		shape.setColor(.0f, .12f, .6f, .474f);
		ShipEntity ship = getShipEntity();
		if (ship != null){
			int x = mapWidth/2, y = mapHeight/2, size = (int) shieldRadius, tot = 8;
			for (int i = 0; i < ship.shield; i++){
				float rotateSpeed = 40f;
				q.set(size + i * SHIELD_SPACING, 0);
				int dir = 1;
				if (i % 2 == 0) dir = -1;
				q.rotate((stateTime * rotateSpeed + (((360f/tot)/ship.shield)/1)*i ) * dir);
				for (int k = 0; k < tot; k++){
					q.rotate(360/tot);
					r.set(q);
					r.rotate(360/tot);
					shape.line(q.x + x, q.y + y, r.x + x, r.y + y);
				}
			}
			
		}

		shape.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glLineWidth(1f);
		shape.setProjectionMatrix(wcamera.combined);
		shape.setColor(1f, 1f, 1f, 1f);
		shape.begin(ShapeType.Line);
		if (alignment == Alignment.CENTRE){
			UISystemButton button = ui.shipSystemottomButtons[lastPoweredSystem];
			vec2.set(0, button.getY());
			button.localToStageCoordinates(vec2);
			float x0 = vec2.x;
			float y0 = wcamera.viewportHeight - button.getHeight() - 1;
			float x1 = x0 + button.getWidth();
			float y1 = y0;
			shape.line(x0,  y0, x1, y1);
		}
		
		Entity ent = ui.getEntity();
		if (ent != null){
			UIActionButton button = ui.entityActionButtons[ent.actionIndexForPath];
			vec2.set(0, button.getY());
			button.localToStageCoordinates(vec2);
			float x0 = vec2.x;
			float y0 = button.getHeight() + 1;
			float x1 = x0 + button.getWidth();
			float y1 = y0;
			shape.line(x0,  y0, x1, y1);
		}
		
		shape.end();
		shape.setProjectionMatrix(camera.combined);
		
		
	}
	//Vector2 vec2 = new Vector2();
	public void drawTargettedLines(ShapeRenderer shape, UI ui, Ship playerShip) {
		shape.begin(ShapeType.Line);
		for (Entity e : playerShip.entities){
			if (e instanceof Weapon){
				Weapon w = (Weapon) e;
				if (w.hasTarget){
					int x = w.target.x, y = w.target.y, size = 10;
					q.set(size, 0);
					for (int i = 0; i < 6; i++){
						q.rotate(60);
						r.set(q);
						r.rotate(30);
						shape.line(q.x + x, q.y + y, r.x + x, r.y + y);
					}
					
				}
				
			}
		}
		
		shape.end();
	}

	public void updateCamera(OrthographicCamera wcamera, World world) {
		//camera.zoom = mapHeight / camera.viewportHeight;
		int otherWidth = 0;
		if (alignment == Alignment.CENTRE){
			//Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			v.set(0, 0, 0);
			Ship otherShip = world.getEnemyShip();
			if (otherShip != null){
				otherShip.camera.project(v);
				otherWidth = (int) (camera.viewportWidth - v.x);
				//Gdx.app.log(TAG, "otherwidth  " + otherWidth);
			}
			//Gdx.gl.glScissor(0, 0, width, Gdx.graphics.getHeight());
		}
		float beta =  camera.zoom / maxZoomForCentering;
		//Gdx.app.log(TAG, "beta " + beta + " max " + maxZoomForCentering);
		camera.setToOrtho(false, wcamera.viewportWidth, wcamera.viewportHeight);
		
		if (camera.zoom > maxZoomForCentering) {
			camera.zoom = maxZoomForCentering;
			offset.scl(.75f);		
		}
		
		//offset.set(0, 0);
		//camera.position.set(wcamera.position);
		//camera.update();
		float hMapW = mapWidth/2, hMapH = mapHeight / 2, hScreenW = camera.viewportWidth/2, hScreenH = camera.viewportHeight/2;
		switch (alignment){
		case CENTRE:
			//otherWidth = 0;
			//camera.position.x *= (camera.zoom);
			camera.position.set(0, 0, 0);
			camera.translate(camera.zoom * otherWidth/2, 0);
			camera.translate(offset);
			camera.translate(hMapW, hMapH);
			//camera.translate(, 0);
			//camera.position.x = Math.min(Math.max(0+camera.zoom * otherWidth/2, camera.position.x), (mapWidth) + camera.zoom * otherWidth/2);
			camera.position.y = Math.min(Math.max(0, camera.position.y), mapHeight);
			camera.update();
			
			v.set(0, 0, 0);
			
			camera.project(v);
			//Gdx.app.log(TAG, "translate " + v);
			break;
		case TOP_RIGHT:
			if (zoomPause > 0) {
				zoomPause -= Gdx.graphics.getDeltaTime();
			} else {
				
				if (zoomingIn) {
					zoomAlpha += Gdx.graphics.getDeltaTime() * ZOOM_SPEED;
					if (zoomAlpha > 1) {
						zoomAlpha = 1;
						zoomingIn = false;
					}
				}else
					if (zoomingOut) {
						zoomAlpha -= Gdx.graphics.getDeltaTime() * ZOOM_SPEED;
						if (zoomAlpha < 0) {
							zoomAlpha = 0;
							zoomingOut = false;
						}
					}
			}
			
			camera.zoom = MathUtils.lerp(zoomedOutEnemyZoom, zoomInTarget, smoothStep(smoothStep(zoomAlpha)));
			camera.position.set(0, 0, 0);
			//camera.zoom = 1f;
			//camera.translate(offset);
			camera.translate(0, hMapH);
			camera.translate(-(camera.viewportWidth/2f) * camera.zoom + mapWidth, 0);
			//Gdx.app.log(TAG, "camera  " + camera.viewportWidth/2 + " zoom " + camera.zoom);
			camera.update();
			break;
		}
		//Gdx.app.log(TAG, "ccam " + camera.position);
	}
	
	public float smoothStep(float t) {
		return t*t * (3f - 2f*t);
	}
	
	public void updateEntities(World world, UI ui) {
		tick++;
		for (int i = entities.size - 1; i >= 0; i--){
			entities.get(i).update(world, ui);
		}
	}

	public void removeEntity(Entity entity) {
		if (entity == null) return;
		Pools.free(entities.removeValue(entity, true));
	}
	public void removeEntityNoPool(Entity entity) {
		entities.removeValue(entity, true);
	}

	public void addEntity(Entity e) {
		entities.add(e);
		e.setMap(this);
	}

	public void drag(float dx, float dy) {
		if (alignment == Alignment.CENTRE)
			offset.add(dx * camera.zoom, dy * camera.zoom);
	}
	public void swap(int a, int b) {
		int aind = 0;
		for (int i = 0; i < systemButtonOrder.length; i++) if (systemButtonOrder[i] == a) aind = i;
		int bind = 0;
		for (int i = 0; i < systemButtonOrder.length; i++) if (systemButtonOrder[i] == b) bind = i;
		int c = systemButtonOrder[aind];
		//Gdx.app.log(TAG, "SWAP " + aind + "  " + bind);
		//Gdx.app.log(TAG, "SWVA " + systemButtonOrder[aind] + "  " + systemButtonOrder[bind]);
		systemButtonOrder[aind] = systemButtonOrder[bind];
		systemButtonOrder[bind] = c;
	}

	public void updateBlocks() {
		for (int i = 0; i < 64; i++)
			map.updateBlocks(this);
		map.updateAirNew(wire, this);
		//wire.clear();
	}

	public EntityArray getEntities() {
		return entities;
	}
	public void savePreview(FileHandle file, Ship ship) {
		Pixmap pixmap = new Pixmap(mapWidth, mapHeight, Format.RGB888);
		int x0, x1, y0, y1;
		x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
				FrameBuffer frameb = chunkBuffer[x + y * chunksX];
				frameb.begin();
				Pixmap frame  = ScreenUtils.getFrameBufferPixmap(0, 0, frameb.getWidth(), frameb.getHeight());
				
				pixmap.drawPixmap(frame, (int)((float)x * CHUNKSIZE)
					, pixmap.getHeight() - y * CHUNKSIZE);
				frameb.end();
				//if (!frameb.getColorBufferTexture().getTextureData().isPrepared())frameb.getColorBufferTexture().getTextureData().prepare();;
				//Pixmap frame = frameb.getColorBufferTexture().getTextureData().co;;
				//for (int g = 0; g < frame.getWidth(); g++){
				//	Gdx.app.log(TAG, ""+frame.getPixel(g,  3));
				//}
				
				//if (frameb != null) pixmap.drawPixmap(frame, (int)((float)x * CHUNKSIZE)
				//		, pixmap.getHeight() - y * CHUNKSIZE);
				
				//drawChunk(x, y, batch, drawWires, drawFill);
				//drawn.set(x + y * chunksX);
			}
		//batch.draw(img, 0, 0);
		
		PixmapIO.writePNG(file, pixmap);
	}
	
	public static class EntityArray extends Array<Entity>{
		
	}

	public void load(IntPixelMap map2, EntityArray entities2, Texture hull2, IntArray inv) {
		Gdx.app.log(TAG, "loadddd");
		for (Entity e : entities){
			Pools.free(e);
		}
		entities.clear();
		Pools.free(entities);
		entities = entities2;
		Gdx.app.log(TAG, "size " + entities.size);

		for (Entity e : entities)
			Gdx.app.log(TAG, "ENTITY " + e);
		map = map2;
		if (mapWidth != map.width || mapHeight != map.height){
			fill = new IntPixelMap(map);
			wire = new IntPixelMap(map);
			mapWidth = map.width;
			mapHeight = map.height;
			
			shieldRadius = Math.max(mapWidth/2,  mapHeight/2);
			shieldRadius *= 1.5f;
			shieldRadius2 = shieldRadius * shieldRadius;
			chunksX = mapWidth / CHUNKSIZE + (mapWidth % CHUNKSIZE == 0?0:1);
			chunksY = mapHeight / CHUNKSIZE + (mapHeight % CHUNKSIZE == 0?0:1);
			Gdx.app.log(TAG, "NEW HELPER CHUNkS");
		}
		hull.dispose();
		if (hull2 != null){
			hull.setTexture(hull2);			
		}
		Gdx.app.log(TAG, "size " + entities.size);

		System.gc();
		Gdx.app.log(TAG, "size " + entities.size);
		for (int i = 0; i < entities.size; i++){
			Entity e = entities.get(i);
			Gdx.app.log(TAG, "ent " + i + " : " + e + " / " + entities.size);
			//if (e instanceof ShipEntity){
				//Gdx.app.log(TAG, "remove AI " + e.getClass() + e.glyph);
			//	removeEntity(e);
			//}
			//else
			{
				e.ship = this;
				e.setDefaultAI();
                //if (e instanceof Door)
                   // Gdx.app.log(TAG, "DEFAULT AI " + e.getClass() + e.glyph);
			}
		}
		for (int i = entities.size-1; i >= 0; i--) {
			Entity e = entities.get(i);
			Gdx.app.log(TAG, "ent " + i + " : " + e + " / " + entities.size);
			if (e instanceof ShipEntity){
				Gdx.app.log(TAG, "remove AI " + e.getClass() + e.glyph);
				removeEntity(e);
			}
		}
		
		inventory.clear();
		inventory.addAll(inv);
		
		//setAllDirty();
		hasCategorizedBlocks = false;
		hasCalculatedConnectivity = false;
		populateRandomChunkOrder();
		//aStar = new AStar2(mapWidth, mapHeight, this);
		depleter = new AStarDeplete(mapWidth, mapHeight, this);

		cacheVerts = new float[chunksX * chunksY][CHUNKSIZE * CHUNKSIZE * 3 * 4];
		cacheDrawCount = new int[chunksX * chunksY];


	}

	

	private void populateRandomChunkOrder() {
		int x0, x1, y0, y1;
		x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
		for (GridPoint2 g : chunksInRandomOrder) Pools.free(g);
		chunksInRandomOrder.clear();
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
				GridPoint2 pt = Pools.obtain(GridPoint2.class);
				pt.set(x, y);
				chunksInRandomOrder.add(pt);;
			}
		chunksInRandomOrderForCaching.clear();
		chunksInRandomOrderForCaching.addAll(chunksInRandomOrder);
		
	}
	
	public void addWeapon(int x, int y) {
		Weapon weapon = Pools.obtain(Weapon.class);
		weapon.clear();
		int currentWeaponCount = 0;
		weapon.x = x;
		weapon.y = y;
		weapon.setDefaultAI();
		addEntity(weapon);
		for (Entity e : entities) if (e instanceof Weapon) ((Weapon)e).setIndex(currentWeaponCount++);
		Gdx.app.log(TAG, "ADD WEAPON");
	}
	private static Vector2 vec2 = new Vector2();
	public void deleteWeapon(int x, int y) {
		Weapon w = null;
		float dst = 100000000;
		vec2.set(x, y);
		for (Entity e : entities) if (e instanceof Weapon){
			Weapon weap = ((Weapon)e);
			float d = vec2.dst2(e.x, e.y);
			if (d < dst){
				dst = d;
				w = weap;
			}
		}
		if (w != null){
			removeEntity(w);
		}
	}

	public void placeSpawn(int x, int y) {
		map.spawn.set(x, y);
	}

	public void placeDoor(int x, int y, int radius) {
		Door door = Pools.obtain(Door.class);
		door.clear();
		int currentWeaponCount = 0;
		door.x = x;
		door.y = y;
		door.radius = radius;
		door.setDefaultAI();
		addEntity(door);
		//for (Entity e : entities) if (e instanceof Weapon) ((Weapon)e).setIndex(currentWeaponCount++);

		int s = door.radius, h = s/2;

		for (int bx = x - h; bx < x -h+s; bx++)
			for (int by = y - h; by < y -h+s; by++){

				int block = map.get(bx, by);
				int id = block & Ship.BLOCK_ID_MASK;
				BlockDef def = IntPixelMap.defs[id];
				if (id == WALL){
					id = DOOR;
					block &= ~BLOCK_ID_MASK;
					block |= id;
					block &= ~BLOCK_BOOST_MASK;
					block |= 1 << BLOCK_EXTRA_BITS;

					map.set(bx, by, block);
				}
			}

		Gdx.app.log(TAG, "ADD DOOR");
	}

	public void deleteDoor(int x, int y) {
		Door w = null;
		float dst = 100000000;
		vec2.set(x, y);
		for (Entity e : entities) if (e instanceof Door){
			Door door = ((Door)e);
			float d = vec2.dst2(e.x, e.y);
			if (d < dst){
				dst = d;
				w = door;
			}
		}
		if (w != null){
			removeEntity(w);
		}
	}
	public boolean[][] roomsConnected = new boolean[100][100];
	public void calculateConnectivity(World world){
		//Gdx.app.log(TAG, "connectivity ");
		for (int i = 0; i < maxSysRoomID; i++){
			GridPoint2 bl = roomBlocks[i];
			for (int k = 0; k < maxRoomID; k++){
				roomsConnected[i][k] = false;
				GridPoint2 tar = roomBlocks[k];
				IntArray path = aStar.getPath(bl.x, bl.y, tar.x, tar.y);
				if (path.size > 0 || i == k){
					Gdx.app.log(TAG, "connected " + bl + i + k);
					roomsConnected[i][k] = true;
				}
				Pools.free(path);
			}

		}
		hasCalculatedConnectivity = true;
	}
	
	public void categorizeSystems() {
		roomBlocks = new GridPoint2[1000];

		for (int i = 0; i <systemNames.length; i++){
			if (systemBlocks[i] == null) systemBlocks[i] = new Array<GridPoint2>();
			for (int r = 0; r < systemBlocks[i].size; r++) Pools.free(systemBlocks[i].get(r));
			systemBlocks[i].clear();
		}
		for (int x = 1; x < mapWidth - 1; x++){
			for (int y = 1; y < mapHeight - 1; y++){
				int block = map.get(x, y);
				int id = (block &BLOCK_ID_MASK);
				if (id != VACCUUM && id != FLOOR && id != WALL){
					GridPoint2 p = Pools.obtain(GridPoint2.class);
					p.set(x, y);
					systemBlocks[id].add(p);
				}
			}
		}
		for (int i = 0; i <systemNames.length; i++){
			systemBlocks[i].shuffle();
		}
		hasCategorizedBlocks = true;
		
		//Gdx.app.log(TAG, "fill ");
		
		int roomID = 1, sysRoomID = 0;
		room.clear(-1);
		systemRooms.clear(-1);
		for (int i = 0; i < roomsBySystem.length; i++){
			roomsBySystem[i].clear();;
		}
		//map.setAllBoosted();

		for (int x = 1; x < mapWidth - 1; x++)
			for (int y =1; y < mapHeight-1; y++){
				int id = map.get(x,  y) &BLOCK_ID_MASK;
				if (id !=WALL && id !=VACCUUM && id !=FLOOR && id != DOOR)
					map.needsBoost[id].put(x + y * mapWidth, 0);
				
				int fillB = room.get(x, y) ;
				if (fillB == -1){

					if (id == DOOR){
						room.set(x, y, 0);
					}
					else if (id ==WALL || id ==VACCUUM){
						//Gdx.app.log(TAG, "fill " + x);
						room.set(x, y, -2);
					} else {
						room.floodFillWalkable(map, x, y, roomID++);
						//room.set(x, y, -2);
						//Gdx.app.log(TAG, "set -2 : " + systemNames[id]);
					}

				}


				int sysRoomB = systemRooms.get(x,  y);
				if (sysRoomB == -1 && id !=WALL && id !=VACCUUM ){
					//Gdx.app.log(TAG, "sysFill " + id);
					systemRooms.floodFillSystem(map, x, y, id, sysRoomID);
					roomsBySystem[id].add(sysRoomID);
					if (roomBlocks[sysRoomID] == null)
						roomBlocks[sysRoomID] = new GridPoint2();
					roomBlocks[sysRoomID].set(x, y);
					sysRoomID++;
				}
			}
		maxSysRoomID = sysRoomID;
		maxRoomID = roomID;
		//GridPoint2 average = Pools.obtain(GridPoint2.class);
		for (int i = 0; i < average.length; i++){
			if (average[i] == null){
				average[i] = new GridPoint2();
			}
			average[i].set(0, 0);
			averageTotals[i] = 0;
		}
		for (int x = 1; x < mapWidth - 1; x++)
			for (int y =1; y < mapHeight-1; y++){
				int id = map.get(x,  y) & BLOCK_ID_MASK;
				average[id].add(x, y);					
				averageTotals[id]++;
				
				
			}
		for (int i = 0; i < average.length; i++){
			if (averageTotals[i] == 0){//no blocks
				//average[i].set(0, 0);
			} else {
				average[i].x /= averageTotals[i];
				average[i].y /= averageTotals[i];
				int id = map.get(average[i].x,  average[i].y) & BLOCK_ID_MASK;
				if (id != i){//look for closest block with right id
					float dist = 1000000000;
					GridPoint2 distPt = Pools.obtain(GridPoint2.class);
					distPt.set(-1, -1);
					for (int x = 1; x < mapWidth - 1; x++)
						for (int y =1; y < mapHeight-1; y++){
							if ((map.get(x,  y) & BLOCK_ID_MASK) == id ){
								float newD = average[i].dst2(x, y);
								if (newD < dist){
									dist = newD;
									distPt.set(x, y);
								}
							}
						}
					if (distPt.x != -1){
						average[i].set(distPt);
					}
					Pools.free(distPt);;
					distPt = null;
				}
			}
		}
		//Pools.free(average);;
		//average = null;*/
		/*for (;;){

			room[x + y * mapWidth] = roomID;
		}*/
		
	}
	GridPoint2[] average = new GridPoint2[systemNames.length];
	int[] averageTotals = new int[systemNames.length];

	public void selectClosestEntity(int x, int y, UI ui, Ship shipB, int x2, int y2) {
		Entity w = null;
		float dst = 100000000;
		vec2.set(x, y);
		for (Entity e : entities) {
			if (e.isHostile) continue;
			float d = vec2.dst2(e.x, e.y);
			if (d < dst){
				dst = d;
				w = e;
			}
		}
		vec2.set(x2, y2);
		for (Entity e : shipB.entities) {
			if (!e.isHostile) continue;
			float d = vec2.dst2(e.x, e.y);
			if (d < dst){
				dst = d;
				w = e;
			}
		}
		//Gdx.app.log(TAG, "select closest e");
		if (w != null){
			selectEntity(w, ui);
		}
	}

	private void selectEntity(Entity e, UI ui) {
		ui.setEntity(e);
		selectedEntity = e;
		//Gdx.app.log(TAG, "select e");
	}

	public void setWeaponTarget(int index, int x, int y) {
		Weapon w = getWeapon(index);
		if (w != null){
			w.target.set(x, y);
			w.hasTarget = true;
		}
		
		//Gdx.app.log(TAG, "zoom out for target");
	}
	
	
	public void cancelWeaponTarget(int index){
		Weapon w = getWeapon(index);
		if (w != null){
			w.hasTarget = false;
		}
	}

	private Weapon getWeapon(int index) {
		for (Entity e : entities) {
			if (e instanceof Weapon){
				Weapon w = (Weapon) e;
				//Gdx.app.log(TAG, "look w " + w.index);
				if (w.index == index) return w;
			}
		}
		//Gdx.app.log(TAG, "get w " + index + "  " + inventory.size);
		return null;
	}

	public void equipWeapon(int index, ItemButton item) {
		unequipWeapon(item.index);
		Weapon w = getWeapon(index);
		w.equip(item);
		
	}

	private void unequipWeapon(int itemIndex) {
		for (Entity e : entities) {
			if (e instanceof Weapon){
				Weapon w = (Weapon) e;
				if (w.equippedItemIndex == itemIndex){
					w.unequip();
				}
			}
		}
	}
	public Laser[] deployedLasers = new Laser[ItemDisplay.MAX_WEAPONS];
	
	public void shoot(WeaponItem weI, GridPoint2 target, Ship map, Weapon w) {
		switch (weI.weaponType){
		case laser:
			//Gdx.app.log(TAG, "shoot");
			if (deployedLasers[w.index] == null){
				deployedLasers[w.index] = Pools.obtain(Laser.class);
				deployedLasers[w.index].index = w.index;
				deployedLasers[w.index].setDefaultAI();
				deployedLasers[w.index].target.set(target);
				deployedLasers[w.index].x = w.x;
				deployedLasers[w.index].y = w.y;
                deployedLasers[w.index].weaponItemID = w.equippedItemID;
                //deployedLasers[w.index].target.set(target.x, target.y);
				addEntity(deployedLasers[w.index]);

			} else {
                deployedLasers[w.index].shoot();
            }
            deployedLasers[w.index].time = 0;


			break;
		case missile:
			Missile miss = Pools.obtain(Missile.class);
			miss.x = w.x;
			miss.y = w.y;
			miss.position.set(w.x, w.y);;
			miss.target.set(target.x, target.y);
			miss.setDefaultAI();
			miss.weaponItemID = w.equippedItemID;
			addEntity(miss);
			break;
		}
	}

	public void removeLaser(int index, Entity e) {
		removeEntity(e);
		deployedLasers[index] = null;
	}
	private Bits reserved = new Bits();
	private int lastPoweredSystem;
	public float stateTime;
	public float shieldRadius2, shieldRadius;
	public boolean showHull = true;
	private float zoomPause;
	private int nextGlyphIndex;
	private boolean isHostile;
	public void unReserve(int x, int y) {
		//Gdx.app.log(TAG, "unresrv " + x + "," + y);
		//if (!reserved.get(x + y * mapWidth)) Gdx.app.log(TAG, "dfka");
		reserved.clear(x + y*mapWidth);
	}
	public void reserve(int x, int y){
		//if (reserved.get(x + y * mapWidth)) Gdx.app.log(TAG, "Already reserved" + x + "," + y);
		//else Gdx.app.log(TAG, "reserve " + x + "," + y);
		reserved.set(x + y*mapWidth);
	}
	public boolean isReserved(int x, int y){
		return reserved.get(x + y * mapWidth);
	}
	public void clearReserved(){
		reserved.clear();
	}

	public boolean hasShipEntity() {
		for (int i = 0; i < entities.size; i++)
			if (entities.get(i) instanceof ShipEntity) return true;
		return false;
	}

	public void unReserveAll() {
		for (int x = 0; x < mapWidth ; x++)
			for (int y =0; y < mapHeight; y++){
				unReserve(x, y);
			}
	}

	public ShipEntity getShipEntity() {
		for (int i = 0; i < entities.size; i++)
			if (entities.get(i) instanceof ShipEntity) return (ShipEntity) entities.get(i);
		
		return null;
	}

	public void setSystemMarker(int maxSystem) {
		lastPoweredSystem = maxSystem;
		
	}

	public void dispose() {
		hull.dispose();
		
	}

	public int getReservedCount() {
		int ind = reserved.nextSetBit(0), count =0;;
		while (ind != -1){
			ind = reserved.nextSetBit(ind+1);
			count++;
		}
		return count;
	}
	
	public void ensureValidSpawnPoint() {
		int curr = map.get(map.spawn.x, map.spawn.y) & BLOCK_ID_MASK;
		if (curr == VACCUUM || curr == WALL){
			int found = 0, count = 0, dist = 100000;
	
			while (found < 5 && count++ < 1000){
				int x = MathUtils.random(mapWidth-1);
				int y = MathUtils.random(mapHeight-1);
				int di = Math.abs(x - mapWidth/2) + Math.abs(y - mapHeight/2);
				int dblock = map.get(x,  y) & BLOCK_ID_MASK;
				if (dblock != WALL && dblock != VACCUUM){
					found++;
					if (di < dist){
						dist = di;
						point.set(x, y);					
					}
				}
			}
			if (found > 0){
				map.spawn.set(point);
			} else Gdx.app.log(TAG, "FAILED TO FIND A SPAWN");
			
		}
			
	}
	
	public void offsetForZoom(float x, float y) {
		if (camera.zoom < maxZoomForCentering-.02f)
			offset.sub(x, y);
	}
	public void zoomInForTarget() {
		zoomingIn = true;
		zoomingOut = false;
	}	
	void zoomOutForTarget() {
		zoomingIn = false;
		zoomingOut = true;
		zoomPause = .15f;
	}
	public void addEntity(int raceIndex) {
		Entity e = Pools.obtain(Entity.class);
		e.setDefaultAI();
		e.font = raceIndex;
		e.glyph = getNextAvailableGlyph();
		e.pos(map.spawn);
		e.setDefaultButtonOrder();
		addEntity(e);
	}
	private String getNextAvailableGlyph() {
	
		String glyph = World.letters[nextGlyphIndex++ % World.letters.length];
		return glyph;
	}
	public void doCommand(String cmd, GameInfo info, UI ui, World world) {
		
		if (cmd.equals("reward")) {
			Gdx.app.log(TAG, "REWARD");
			
		} else if (cmd.substring(0, 5).contains("spawn")) {
			String shipName = cmd.split(" ")[1];
			Gdx.app.log(TAG, "SPAWN " + shipName);
			Ship enemy = world.getEnemy(this);
			world.loadShip(shipName, enemy);
		} else if (cmd.equals("hostile")) {
			Gdx.app.log(TAG, "HOSTILE");
			Ship enemy = world.getEnemy(this);
			if (enemy != null) {
				enemy.setHostile(true);
				setHostile(true);
			}
		}
		
		
		
		else Gdx.app.log(TAG, "FAILED command " + cmd);
	}
	
	private void setHostile(boolean b) {
		isHostile = b;
	}
	public void setForNewGamePreview() {
		map.unBoostAll();
		map.setAirForNewGame();
	}
	
}
