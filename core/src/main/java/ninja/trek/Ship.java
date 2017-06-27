package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ScreenUtils;

import ninja.trek.ui.ItemDisplay;
import ninja.trek.ui.ItemDisplay.ItemButton;
import ninja.trek.ui.UI;

public class Ship {
	private static final String TAG = "pixel canvas";
	public static final int BLOCK_ID_MASK = 0x007f
			, BLOCK_BOOST_BITS = 7, BLOCK_BOOST_MASK = 0x01 << BLOCK_BOOST_BITS
			, BLOCK_DAMAGE_BITS = 8, BLOCK_DAMAGE_MASK = 0x0f << BLOCK_DAMAGE_BITS
			, BLOCK_AIR_BITS = 12, BLOCK_AIR_MASK = 0x1ff << BLOCK_AIR_BITS
			, BLOCK_FIRE_BITS = 21, BLOCK_FIRE_MASK = 0x03 << BLOCK_FIRE_BITS
			, BLOCK_DATA_BITS = 23, BLOCK_DATA_MASK = 0xff << BLOCK_DATA_BITS;
			
			;
	public static final int MAX_DAMAGE = 15;

	public enum Alignment {CENTRE, TOP_RIGHT};
	public Alignment alignment = Alignment.CENTRE;
	public int[] systemButtonOrder = new int[systemNames.length];
	public int[] maxDepletionBySystem = {64, 64, 64, 64, 64, 64, 64, 64, 64};
	public float[] maxDamgeBySystem =   {15, 15, 15, 15, 15, 15, 15, 15, 15};;
	Array<GridPoint2> queue = new Array<GridPoint2>();
	public boolean placeWeapon = false;
	public boolean deleteWeapon;
	public boolean placeSpawn;
	private int cacheProgress;
	private Array<GridPoint2> chunksInRandomOrder = new Array<GridPoint2>();
	public int tick;
	public static String[] systemNames = {"Vac", "Engine", "Weapon", "Shield", "Wall", "Floor", "Oxygen", "Power"};
	public static final int VACCUUM = 0; 
	public static final int ENGINE = 1;
	public static final int WEAPON = 2;
	public static final int SHIELD = 3;
	public static final int WALL = 4;
	public static final int FLOOR = 5;
	public static final int OXYGEN = 6;
	public static final int POWER = 7;
	protected int[] damageThreshold = {1, 1, 1, 1, 1, 1, 1, 1};
	public final int chunkSize;
	public int mapWidth;
	public int mapHeight;
	public int chunksX;
	public int chunksY;
	private final int cacheIterations;
	private static final int MAP_EXTRA_PIXELS = 64;
	static final float GAP = 10;
	private transient FrameBuffer[] chunkBuffer, fillBuffer, wireBuffer;
	private transient Texture[] chunkTextures, fillTextures, wireTextures;;
	private transient boolean[] dirtyChunk;
	//private int[] map;
	private transient TextureRegion pixel;
	private float backR = .01f//.31f
			, backG = .01f//.31f
			, backB = .01f;//0.1872974f;
	public IntPixelMap map, fill, wire;
	private IntPixelMap room;
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
	public transient boolean drawFill = false;
	public transient boolean drawWires = false;
	public transient boolean editMode = false;
	private transient ShaderProgram shader;
	public AStarDeplete depleter;
	public OuterHull hull = new OuterHull();
	public Array<GridPoint2>[] systemBlocks = new Array[16];
	public boolean hasCategorizedBlocks =false;
	Vector2 q = new Vector2(), r = new Vector2();
	
	public IntArray inventory = new IntArray(true, 8);
	private IntPixelMap systemRooms;
	private IntArray[] roomsBySystem;
	
	public Ship(IntPixelMap map, Sprite pixelSprite, FontManager fonts, ShaderProgram shader){
		this(map, 10, pixelSprite, fonts, shader);
	}
	
	public Ship(IntPixelMap map, int cacheIterations, Sprite pixelSprite, FontManager fonts, ShaderProgram shader){
		if (pixelSprite.getHeight() != pixelSprite.getWidth()) throw new GdxRuntimeException(" prites not square");
		pixelSize = (int) pixelSprite.getHeight();
		this.chunkSize = map.chunkSize;
		this.mapWidth = map.width;
		this.mapHeight = map.height;
		this.cacheIterations = cacheIterations;
		chunksX = mapWidth / chunkSize + (mapWidth % chunkSize == 0?0:1);
		chunksY = mapHeight / chunkSize + (mapHeight % chunkSize == 0?0:1);
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
		//Gdx.app.log(TAG, "map width " + chunksX + "  ,  " + chunksY + "  chunksize " + chunkSize + "  w " + mapWidth);
		chunkBuffer = new FrameBuffer[8 * 8];
		chunkTextures = new Texture[8 * 8];
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
				FrameBuffer buff = new FrameBuffer(Format.RGB888, chunkSize * pixelSize, chunkSize * pixelSize, false);
				return buff;
			}
			
		};
		aStar = new AStar2(mapWidth, mapHeight, this);
		depleter = new AStarDeplete(mapWidth, mapHeight, this);

		for (int i = 0; i < systemButtonOrder.length; i++){
			systemButtonOrder[i] = i;
		}
		populateRandomChunkOrder();

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
		setAllDirty();
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

	private void cacheChunk(int x, int y, SpriteBatch batch, FrameBuffer[] buffer, IntPixelMap map) {
		//Gdx.app.log(TAG, "cache chunk" + x + "," + y);
		int chunkIndex = x + y * chunksX;
		//int[] chunk = chunkData[chunkIndex];
		if (buffer[chunkIndex] == null)
			makeFrameBuffer(chunkIndex);
		buffer[chunkIndex].begin();
		Gdx.gl.glClearColor(backR, backG, backB , 1f);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, chunkSize, chunkSize);
		batch.setColor(Color.WHITE);
		
		batch.begin();
		for (int xx = 0; xx < chunkSize; xx++)
			for (int yy = 0; yy < chunkSize; yy++){
				//int blockIndex = (x*chunkSize + xx) + (y * chunkSize + yy) * chunksX * chunkSize;
				//Gdx.app.log(TAG, "cache block" + blockIndex + "," + x + "," + xx + "," + y + "," + yy + "," );
				//switch (chunkData[blockIndex]){
				int ax = x*chunkSize + xx , ay =  y * chunkSize + yy;
				int block = map.get(ax, ay);
				batch.setColor(map.getColor(block, ax, ay, this));
				batch.draw(pixel, xx, chunkSize - yy-1, 1, 1);
				//if (block != 0) Gdx.app.log(TAG, "col " + map.getColor(block));
			}
		batch.end();
		buffer[chunkIndex].end();
	}

	
	/** 
	 * @param x world x coord
	 * @param y world y coord
	 */
	public void setDirty(int x, int y){
		if (x >= mapWidth || y >= mapHeight || x < 0 || y < 0) return;
		int chunkX = x / chunkSize, chunkY = y / chunkSize, chunkIndex = chunkX + chunkY * chunksX;
		dirtyChunk[chunkIndex] = true;
	}
	
	/** Sets all chunks in a rectangle to dirty
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void setDirty(int x0, int y0, int x1, int y1){
		int chunkX0 = x0 / chunkSize, chunkY0 = y0 / chunkSize;
		int chunkX1 = x1 / chunkSize, chunkY1 = y1 / chunkSize;
		for (int chunkX = chunkX0; chunkX <= chunkX1; chunkX++)
			for (int chunkY = chunkY0; chunkY <= chunkY1; chunkY++){
				int chunkIndex = chunkX + chunkY * chunksX;
				dirtyChunk[chunkIndex] = true;
			}
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera wcamera, World world){
		//batch.getProjectionMatrix().set(camera.combined);
		if (alignment == Alignment.CENTRE){
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			v.set(-GAP, 0, 0);
			Ship otherShip = world.getEnemyShip();
			otherShip.camera.project(v);
		
			int width = (int) v.x;
			Gdx.gl.glScissor(0, 0, width, Gdx.graphics.getHeight());
		}
		updateCamera(wcamera);
		batch.setProjectionMatrix(camera.combined);
		batch.setShader(shader);
		//batch.setShader(null);
		drawn.clear();
		batch.disableBlending();
		batch.setColor(Color.WHITE);
		batch.begin();
		//float[] colorArray = CustomColors.getFloatColorArray();
		//shader.setUniform3fv("u_colors[0]", colorArray , 0, colorArray.length);
		
		int x0, x1, y0, y1;
		x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
				drawChunk(x, y, batch, drawWires, drawFill);
				drawn.set(x + y * chunksX);
			}
		//batch.draw(img, 0, 0);
		batch.end();
		
		toFree.clear();
		toFree.or(hasBuffer);
		toFree.andNot(drawn);
		
		int nextFreeIndex = toFree.nextSetBit(0);
		while (nextFreeIndex != -1) {
			clearFrameBuffer(nextFreeIndex);
			nextFreeIndex = toFree.nextSetBit(nextFreeIndex+1);
		} 
		
		if (alignment == Alignment.CENTRE){
			Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		}
		
		hull.draw(batch, wcamera, world, this);
	}

	private void drawChunk(int x, int y, SpriteBatch batch, boolean wire, boolean fill) {		
		//Gdx.app.log(TAG, "draw chunk" + x + "," + y + "   " + x * CHUNK_SIZE+ "   " + y * CHUNK_SIZE+ "   " + CHUNK_SIZE+ "   " + CHUNK_SIZE);
		//batch.setColor(MathUtils.random());
		Texture texx = chunkTextures[x + y * chunksX];
		if (texx != null) batch.draw(texx, (float)x * chunkSize, y * chunkSize, chunkSize, chunkSize);
		if (wire){
			Texture tex = wireTextures[x + y * chunksX];
			if (tex != null) batch.draw(tex,  (float)x * chunkSize, y * chunkSize, chunkSize, chunkSize);
		}
		if (editMode){
			Texture tex = fillTextures[x + y * chunksX];
			if (tex != null){
				//Gdx.app.log(TAG, "draw fill");
				batch.draw(tex, (float)x * chunkSize, y * chunkSize, chunkSize, chunkSize);
			}
		}
		
	}
	
	public void drawEntities(SpriteBatch batch, World world){
		fonts.setZoom(camera);
		//Gdx.app.log(TAG, "draw entities " + entities.size + "  "  + camera.position);
		batch.setProjectionMatrix(camera.combined);//.translate(offset.x, offset.y, 0);
		batch.setShader(null);
		
		for (Entity e : entities){
			fonts.draw(e, batch, camera);
		}
		
		if (editMode) fonts.drawSpawn(map.spawn, batch);
		
		batch.enableBlending();
		for (Entity e : entities){
			e.draw(batch, camera, world);
		}
		batch.disableBlending();
		//batch.end();
	}
	private Entity selectedEntity;

	public void drawLines(ShapeRenderer shape, UI ui, boolean isSettingTarget){
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
			//v2.set(mapWidth, mapHeight, 0);
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
	}
	
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

	public void updateCamera(OrthographicCamera wcamera) {
		camera.setToOrtho(false, wcamera.viewportWidth, wcamera.viewportHeight);
		//camera.rotate(-90);
		camera.position.set(wcamera.position);
		//camera.zoom = wcamera.zoom;
		camera.update();
		v.set(0, 0, 0);
		camera.unproject(v);
		v.y = camera.viewportHeight - v.y -1;
		//Gdx.app.log(TAG, "offset " + v);
		
		float tw = camera.viewportWidth * camera.zoom ;
		float th = camera.viewportHeight * camera.zoom;
		//w *= camera.zoom;
		
		camera.translate(-v.x, -v.y, 0);
		
		switch (alignment){
		case CENTRE:
			
			break;
		case TOP_RIGHT:
			//Gdx.app.log(TAG, "translate offset " + (w) + "  x  " + (h));
			camera.translate(-(tw - mapWidth) + GAP, -(th - mapHeight) + GAP*8 * camera.zoom, 0);
			break;
		}
		camera.translate(offset);
		camera.update();
	}
	
	public void updateDraw(SpriteBatch batch){
		//update(chunkBuffer, batch);
		queue.clear();
		batch.setShader(null);;
		int cached = 0;
		int tot = chunksX * chunksY;
		int x0, x1, y0, y1;
		x0 = 0; y0 = 0;x1 = chunksX-1;y1 = chunksY-1;
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
			if (
					(!hasBuffer.get(x + y * chunksX))
					||
					true
					//( dirtyChunk[x + y * chunksX])
					){
				queue.add(Pools.obtain(GridPoint2.class).set(x, y));
			}
		}
		
		for (int i = 0; i < cacheIterations; i++){
			cacheProgress++;
			if (cacheProgress >= chunksInRandomOrder.size) cacheProgress = 0;
			if (queue.size == 0) break;
			GridPoint2 pt = chunksInRandomOrder.get(cacheProgress);
			cacheChunk(pt.x, pt.y, batch, chunkBuffer, map);
			cacheChunk(pt.x, pt.y, batch, fillBuffer, fill);
			//cacheChunk(pt.x, pt.y, batch, wireBuffer, wire);
			//dirtyChunk[pt.x + pt.y * chunksX] = false;
			cached++;
		}
	}
	
	public void updateEntities(World world) {
		tick++;
		for (Entity e : entities){
			e.update(world);
		}
	}

	public void removeEntity(Entity entity) {
		Pools.free(entities.removeValue(entity, true));
		
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
		for (int i = 0; i < 512; i++)
			map.updateBlocks(this);
	}

	public void setAllDirty() {
		for (int i = 0; i < dirtyChunk.length; i++)
			dirtyChunk[i] = true;
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
				
				pixmap.drawPixmap(frame, (int)((float)x * chunkSize)
					, pixmap.getHeight() - y * chunkSize);
				frameb.end();
				//if (!frameb.getColorBufferTexture().getTextureData().isPrepared())frameb.getColorBufferTexture().getTextureData().prepare();;
				//Pixmap frame = frameb.getColorBufferTexture().getTextureData().co;;
				//for (int g = 0; g < frame.getWidth(); g++){
				//	Gdx.app.log(TAG, ""+frame.getPixel(g,  3));
				//}
				
				//if (frameb != null) pixmap.drawPixmap(frame, (int)((float)x * chunkSize)
				//		, pixmap.getHeight() - y * chunkSize);
				
				//drawChunk(x, y, batch, drawWires, drawFill);
				//drawn.set(x + y * chunksX);
			}
		//batch.draw(img, 0, 0);
		
		PixmapIO.writePNG(file, pixmap);
	}
	public static class EntityArray extends Array<Entity>{
		
	}

	public void load(IntPixelMap map2, EntityArray entities2) {
		for (Entity e : entities){
			Pools.free(e);
		}
		entities.clear();
		entities = entities2;
		map = map2;
		if (mapWidth != map.width || mapHeight != map.height){
			fill = new IntPixelMap(map);
			wire = new IntPixelMap(map);
			mapWidth = map.width;
			mapHeight = map.height;
			chunksX = mapWidth / chunkSize + (mapWidth % chunkSize == 0?0:1);
			chunksY = mapHeight / chunkSize + (mapHeight % chunkSize == 0?0:1);
			Gdx.app.log(TAG, "NEW HELPER CHUNkS");
		}
		
		System.gc();
		for (Entity e : entities){
			if (e instanceof ShipEntity) removeEntity(e);
			else {
				e.ship = this;
				e.setDefaultAI();
				
			}
		}
		
		setAllDirty();
		hasCategorizedBlocks = false;
		populateRandomChunkOrder();
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
	
	public void categorizeSystems() {
		for (int i = 0; i < Ship.systemNames.length; i++){
			if (systemBlocks[i] == null) systemBlocks[i] = new Array<GridPoint2>();
			for (int r = 0; r < systemBlocks[i].size; r++) Pools.free(systemBlocks[i].get(r));
			systemBlocks[i].clear();
		}
		for (int x = 1; x < mapWidth - 1; x++){
			for (int y = 1; y < mapHeight - 1; y++){
				int block = map.get(x, y);
				int id = (block & Ship.BLOCK_ID_MASK);
				GridPoint2 p = Pools.obtain(GridPoint2.class);
				p.set(x, y);
				systemBlocks[id].add(p);
			}
		}
		for (int i = 0; i < Ship.systemNames.length; i++){
			systemBlocks[i].shuffle();
		}
		hasCategorizedBlocks = true;
		
		//Gdx.app.log(TAG, "fill ");
		
		int roomID = 0, sysRoomID = 0;
		room.clear(-1);
		systemRooms.clear(-1);
		for (int i = 0; i < roomsBySystem.length; i++){
			roomsBySystem[i].clear();;
		}
		map.setAllBoosted();
		for (int x = 1; x < mapWidth - 1; x++)
			for (int y =1; y < mapHeight-1; y++){
				int id = map.get(x,  y) & Ship.BLOCK_ID_MASK;
				if (id != Ship.WALL && id != Ship.VACCUUM && id != Ship.FLOOR)
					map.boosted[id].clear(x + y * mapWidth);
				
				int fillB = room.get(x, y) & BLOCK_ID_MASK;
				if (fillB == -1 && id != Ship.WALL && id != Ship.VACCUUM){
					//Gdx.app.log(TAG, "fill " + x);
					room.floodFillWalkable(map, x, y, roomID++);
				}
				int sysRoomB = systemRooms.get(x,  y);
				if (sysRoomB == -1 && id != Ship.WALL && id != Ship.VACCUUM){
					//Gdx.app.log(TAG, "sysFill " + id);
					systemRooms.floodFillSystem(map, x, y, id, sysRoomID);
					roomsBySystem[id].add(sysRoomID);
					sysRoomID++;
				}
			}
		
		
		
		/*for (;;){

			room[x + y * mapWidth] = roomID;
		}*/
		
		
		
	}

	public void selectClosestEntity(int x, int y, UI ui) {
		Entity w = null;
		float dst = 100000000;
		vec2.set(x, y);
		for (Entity e : entities) {
			
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
				if (w.index == index) return w;
			}
		}
		return null;
	}

	public void equipWeapon(int index, ItemButton item) {
		unequipWeapon(item.index);
		Weapon w = getWeapon(item.index);
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
				addEntity(deployedLasers[w.index]);
			}
			deployedLasers[w.index].time = 0;
			break;
		}
	}

	public void removeLaser(int index, Entity e) {
		removeEntity(e);
		deployedLasers[index] = null;
	}
	private Bits reserved = new Bits();
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
}