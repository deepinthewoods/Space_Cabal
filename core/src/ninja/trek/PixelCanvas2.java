package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class PixelCanvas2 {
	private static final String TAG = "pixel canvas";
	public final int chunkSize;
	public final int mapWidth;
	public final int mapHeight;
	public final int chunksX, chunksY;
	private final int cacheIterations;
	private static final int MAP_EXTRA_PIXELS = 64;
	private FrameBuffer[] chunkBuffer;
	private Texture[] chunkTextures;
	private boolean[] dirtyChunk;
	//private int[] map;
	private TextureRegion pixel;
	private float backR = .01f//.31f
			, backG = .01f//.31f
			, backB = .01f;//0.1872974f;
	private IntPixelMap map;
	private Bits drawn = new Bits(), hasBuffer = new Bits(), toFree = new Bits();
	public Pool<FrameBuffer> bufferPool;
	Vector3 vec = new Vector3();
	private int pixelSize;
	
	public PixelCanvas2(IntPixelMap map, Sprite pixelSprite){
		this(map, 10, pixelSprite);
	}
	
	public PixelCanvas2(IntPixelMap map, int cacheIterations, Sprite pixelSprite){
		if (pixelSprite.getHeight() != pixelSprite.getWidth()) throw new GdxRuntimeException(" prites not square");
		pixelSize = (int) pixelSprite.getHeight();
		this.chunkSize = map.chunkSize;
		this.mapWidth = map.width;
		this.mapHeight = map.height;
		this.cacheIterations = cacheIterations;
		chunksX = mapWidth / chunkSize + (mapWidth % chunkSize == 0?0:1);
		chunksY = mapHeight / chunkSize + (mapHeight % chunkSize == 0?0:1);
		 
		this.map = map;
		//Gdx.app.log(TAG, "map width " + chunksX + "  ,  " + chunksY + "  chunksize " + chunkSize + "  w " + mapWidth);
		chunkBuffer = new FrameBuffer[chunksX * chunksY];
		chunkTextures = new Texture[chunksX * chunksY];
		dirtyChunk = new boolean[chunksX * chunksY];
		for (int i = 0; i < dirtyChunk.length; i++){
			dirtyChunk[i] = true;
		}
		for (int i = 0; i < chunkBuffer.length; i++){
			
		}
		pixel = pixelSprite;
		bufferPool = new Pool<FrameBuffer>(){

			@Override
			protected FrameBuffer newObject() {
				FrameBuffer buff = new FrameBuffer(Format.RGB888, chunkSize * pixelSize, chunkSize * pixelSize, false);
				
				return buff;
			}
			
		};
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
		hasBuffer.set(i);
		dirtyChunk[i] = true;
		return buff;
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
		hasBuffer.clear(i);
		
		ind = hasBuffer.nextSetBit(0); count = 0;
		while (ind != -1){
			count++;
			ind = hasBuffer.nextSetBit(ind+1);
		}
		//Gdx.app.log(TAG, "clear  buffer post" + i + "   / "  + count);
	}

	private void cacheChunk(int x, int y, SpriteBatch batch) {
		//Gdx.app.log(TAG, "cache chunk" + x + "," + y);
		int chunkIndex = x + y * chunksX;
		//int[] chunk = chunkData[chunkIndex];
		if (chunkBuffer[chunkIndex] == null)
			makeFrameBuffer(chunkIndex);
		
		chunkBuffer[chunkIndex].begin();
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
				int block = map.get(x*chunkSize + xx , y * chunkSize + yy);
				batch.setColor(map.getColor(block));
				batch.draw(pixel, xx, chunkSize - yy-1, 1, 1);
			}
		batch.end();
		chunkBuffer[chunkIndex].end();
	}

	
	/** 
	 * @param x world x coord
	 * @param y world y coord
	 */
	public void setDirty(int x, int y){
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
	
	private void drawChunk(int x, int y, SpriteBatch batch, Camera camera) {		
		//Gdx.app.log(TAG, "draw chunk" + x + "," + y + "   " + x * CHUNK_SIZE+ "   " + y * CHUNK_SIZE+ "   " + CHUNK_SIZE+ "   " + CHUNK_SIZE);
		//batch.setColor(MathUtils.random());
		Texture tex = chunkTextures[x + y * chunksX];
		if (tex != null) batch.draw(tex, (float)x * chunkSize, y * chunkSize, chunkSize, chunkSize);
		
	}
	
	
	public void update(SpriteBatch batch, Camera camera){
		int cached = 0;
		int tot = chunksX * chunksY;
		int x0, x1, y0, y1;
		vec.set(0, Gdx.graphics.getHeight() + MAP_EXTRA_PIXELS, -MAP_EXTRA_PIXELS);
		camera.unproject(vec);
		x0 = (int) (vec.x / chunkSize);
		y0 = (int) (vec.y / chunkSize);
		vec.set(Gdx.graphics.getWidth() + MAP_EXTRA_PIXELS, -MAP_EXTRA_PIXELS, 0);
		camera.unproject(vec);
		x1 = (int) (vec.x / chunkSize);
		y1 = (int) (vec.y / chunkSize);
		x0 = Math.min(Math.max(0,  x0), chunksX-1);
		x1 = Math.min(Math.max(0,  x1), chunksX-1);
		y0 = Math.min(Math.max(0,  y0), chunksY-1);
		y1 = Math.min(Math.max(0,  y1), chunksY-1);
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
			if (
					(!hasBuffer.get(x + y * chunksX))
					|| 
					(cached < cacheIterations && dirtyChunk[x + y * chunksX])
					){
				cacheChunk(x, y, batch);
				dirtyChunk[x + y * chunksX] = false;
				cached++;
			}
		}
	}
	
	public void draw(SpriteBatch batch, Camera camera){
		//batch.getProjectionMatrix().set(camera.combined);
		batch.setProjectionMatrix(camera.combined);
		camera.update();
		drawn.clear();
		batch.disableBlending();
		batch.setColor(Color.WHITE);
		batch.begin();
		int x0, x1, y0, y1;
		vec.set(0, Gdx.graphics.getHeight() ,0 );
		camera.unproject(vec);
		x0 = (int) (vec.x / chunkSize);
		y0 = (int) (vec.y / chunkSize);
		//Gdx.app.log(TAG, "unproj 1 " + x0 + "   " + y0);
		vec.set(Gdx.graphics.getWidth() ,0 , 0);
		camera.unproject(vec);
		x1 = (int) (vec.x / chunkSize);
		y1 = (int) (vec.y / chunkSize);
		//Gdx.app.log(TAG, "unproj 2 " + x1 + "   " + y1);
		x0 = Math.min(Math.max(0,  x0), chunksX-1);
		x1 = Math.min(Math.max(0,  x1), chunksX-1);
		y0 = Math.min(Math.max(0,  y0), chunksY-1);
		y1 = Math.min(Math.max(0,  y1), chunksY-1);
		
		for (int x = x0; x <= x1; x++)
			for (int y = y0; y <= y1; y++){
				drawChunk(x, y, batch, camera);
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
		
	}
	
	public static class IntPixelMap{
		private static final String TAG = "int pixel map";
		int[] map;
		public int width, height, chunksX, chunksY;
		private int chunkSize;
		public IntPixelMap(int w, int h, int chunkSize){
			width = w;
			height = h;
			this.chunkSize = chunkSize;
			chunksX = width / chunkSize + 1;
			chunksY = height / chunkSize + 1;
			map = new int[chunksX * chunkSize * chunksY * chunkSize];
			
			for (int i = 0; i < 100030; i++){
				int index = MathUtils.random(map.length-1);
				map[index] = MathUtils.random(4);
			}
		}
		public int get(int x, int y) {
			//Gdx.app.log(TAG, "get " + x + ", " + y);
			if (x >= width || y >= height || x < 0 || y < 0) return 0;
			int blockIndex = (x) + (y) * chunksX * chunkSize;
			return map[blockIndex];
		}
		public void set(int x, int y, int b) {
			if (x >= width || y >= height || x < 0 || y < 0) return ;
			map[x  + y * chunkSize * chunksX] = b;
			
		}
		public Color getColor(int block) {
			//Gdx.app.log(TAG, "get color " + block);
			switch (block){
			case 0: return Color.DARK_GRAY;
			case 1: return Color.GREEN;
			case 2: return Color.BLUE;
			case 3: return Color.CYAN;
			default: return Color.DARK_GRAY;
			}
			
		}
	}
}
