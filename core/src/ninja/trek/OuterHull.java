package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;

import squidpony.squidgrid.AestheticDifference;

public class OuterHull {

	private static final String TAG = "outer hull";

	private DetailedMimicPartial mim;

	private Texture region;

	private Pixmap mainPixmap;
	
	public OuterHull(){
		AestheticDifference diff = new AestheticDifference() {
			
			@Override
			public double difference(int a, int b) {
				
				return 0;
			}
		};
		mim = new DetailedMimicPartial(null);
	}
	
	public void calculate(Ship ship, String name, int reps, int radius, int expandX, int expandPlus, int fadeSize){
		Pixmap pix = new Pixmap(Gdx.files.internal("sources/"+name));
		
		float[] priorities = new float[ship.mapWidth * ship.mapHeight];
		int[] pixels = new int[pix.getWidth() * pix.getHeight()];
		
		for (int i = 0; i < pix.getWidth(); i ++)
			for (int k = 0; k < pix.getHeight(); k++){
				pixels[i + k * pix.getWidth()] = pix.getPixel(i, k);
			}
		int maxRad = Math.max(expandX,  expandPlus);
		float[] blackProb = new float[ship.mapWidth * ship.mapHeight];
		for (int i = 0; i < ship.mapWidth; i ++)
			for (int k = 0; k < ship.mapHeight; k++){
				
				int dist = 0;
				boolean finished = false;
				for (int m = 0; m < expandX && !finished; m++){
					int b0 = ship.map.get(i + m, ship.mapHeight - 1 - k + m);
					int b1 = ship.map.get(i + m, ship.mapHeight - 1 - k - m);
					int b2 = ship.map.get(i - m, ship.mapHeight - 1 - k + m);
					int b3 = ship.map.get(i - m, ship.mapHeight - 1 - k - m);
					if (
							(b0 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b1 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b2 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b3 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							) finished = true;
					dist++;
					
					
				}
				int dist2 = 0;
				finished = false;
				for (int m = 0; m < expandPlus && !finished; m++){
					int b0 = ship.map.get(i, ship.mapHeight - 1 - k + m);
					int b1 = ship.map.get(i + m, ship.mapHeight - 1 - k);
					int b2 = ship.map.get(i - m, ship.mapHeight - 1 - k);
					int b3 = ship.map.get(i, ship.mapHeight - 1 - k - m);
					if (
							(b0 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b1 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b2 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							|| (b3 & Ship.BLOCK_ID_MASK) != Ship.VACCUUM
							) finished = true;
					dist2++;
					
					
				}
				dist = Math.max(dist, dist2);
				//blackProb[i + k * ship.mapWidth] = 1f;
				float alpha = 1.72f;
				//if (dist > 2)Gdx.app.log(TAG, "d  " + dist );//+ isClearPixel(clearBits) + isClearPixel(blackBits));
				if (dist < maxRad - fadeSize){
					alpha = 0f;
				} else if (dist < maxRad){
					alpha = 1f;
				} 
				
				//alpha = Math.min(Math.max(0f,  alpha),  1f);
				//if (alpha > 0)Gdx.app.log(TAG, "alpha " + alpha);
				blackProb[i + k * ship.mapWidth] = alpha ;
				//blackProb[i + k * ship.mapWidth] = 10f;
			}
		int[] result = mim.neoProcess(pixels, pix.getWidth(), pix.getHeight(), ship.mapWidth, ship.mapHeight, reps, radius, false, blackProb);
		
		Pixmap npix;// = new Pixmap(ship.mapWidth, ship.mapHeight, Format.RGBA8888);
		if (mainPixmap == null || mainPixmap.getWidth() != ship.mapWidth || mainPixmap.getHeight() != ship.mapHeight){
			npix = new Pixmap(ship.mapWidth, ship.mapHeight, Format.RGBA8888);			
		} else npix = mainPixmap;
		npix.setColor(Color.CLEAR);
		npix.fill();
		//int clearBits = Color.LIGHT_GRAY.toIntBits();
		int blackBits = Color.rgba8888(Color.BLACK);
		int clearBits = Color.rgba8888(1f, 1f, 1f, 0f);
		//clearBits = Color.toIntBits(255, 255, 255, 0);
		
		
		//Gdx.app.log(TAG, "clear  " +  isClearPixel(clearBits) + isClearPixel(blackBits));

		for (int i = 0; i < ship.mapWidth; i ++)
			for (int k = 0; k < ship.mapHeight; k++){
				if (blackProb[i + k * ship.mapWidth] < .1f 
						&& DetailedMimicPartial.isBlackPixel(result[i + k * ship.mapWidth]))
					result[i + k * ship.mapWidth] = blackBits;
				//else //*/
					//npix.drawPixel(i, k, result[i + k * ship.mapWidth]);
				
			}
		//blackBits = Color.rgba8888(Color.WHITE);
		ship.ensureValidSpawnPoint();
		int nodeX = ship.map.spawn.x, nodeY = ship.map.spawn.y, target = Color.BLACK.toIntBits(), replacement = 1;
		int[] ret = new int[ship.mapWidth * ship.mapHeight];
		floodFill(result, ret, nodeX, nodeY, target, replacement, ship.mapWidth, ship.mapHeight);
		for (int i = 0; i < ship.mapWidth; i ++)
			for (int k = 0; k < ship.mapHeight; k++){
				
				if (ret[i + k * ship.mapWidth] == 0){
					//Gdx.app.log(TAG, "cleared");
					//npix.setColor(Color.WHITE);
					//npix.drawPixel(i, k, blackBits);
					
				} else {
					//Gdx.app.log(TAG, "not cleared" + isClearPixel(clearBits));
					//npix.setColor(Color.BLACK);
					//npix.drawPixel(i, k, Color.RED.toIntBits());
					npix.drawPixel(i, k, result[i + k * ship.mapWidth]);
				}
			}
		candidates.clear();
		for (int i = 1; i < ship.mapWidth-1; i ++)
			for (int k = 1; k < ship.mapHeight-1; k++){
				if (
						isClearPixel(npix.getPixel(i,  k))
						
						){
					if (
							( !DetailedMimicPartial.isBlackPixel(npix.getPixel(i+1,  k))  )
							|| 
							( !DetailedMimicPartial.isBlackPixel(npix.getPixel(i-1,  k))  )
							|| 
							( !DetailedMimicPartial.isBlackPixel(npix.getPixel(i,  k+1))  )
							|| 
							( !DetailedMimicPartial.isBlackPixel(npix.getPixel(i,  k-1))  )
							){
						
						candidates.add(i + k * ship.mapWidth);
					}
					
				}
			}
		for (int c = 0; c < candidates.size; c++){
			int index = candidates.get(c);
			int i = index % ship.mapWidth;
			int k = index / ship.mapWidth;
			npix.drawPixel(i, k, blackBits);
		}
		region = new Texture(npix);
		mainPixmap = npix;
	}
	IntArray candidates = new IntArray();
	private void addNode(int x, int y) {
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(x, y);
		floodOpen.add(pt);
		//Gdx.app.log(TAG, "add node "+ pt + 
				//(isClearPixel(Color.BLACK.toIntBits()))
				//);
	}
	private transient Array<GridPoint2> floodOpen = new Array<GridPoint2>();
	public void floodFill(int[] m, int[] res, int nodeX, int nodeY, int target, int replacement, int width, int height){
		GridPoint2 pt = Pools.obtain(GridPoint2.class);
		pt.set(nodeX, nodeY);
		floodOpen.add(pt);
		while (floodOpen.size > 0){
			GridPoint2 node = floodOpen.pop();
			processFloodFill(m, res, node.x, node.y, target, replacement, 0, width, height);
			Pools.free(node);
		}
	}
	Color tmpC = new Color();
	
	 static Color c = new Color();
	 public static  boolean isClearPixel(int i) {
	//	 c.set(Color.WHITE);
//		 int col = Color.WHITE.toIntBits();
		// c.set(col);
		 
		 c.set(i);
		// Gdx.app.log(TAG, "is clear alpha " + c.a );
		 if (c.a < .1f) return true;
		 return false;//c.r < .1f && c.g < .1f && c.b < 0.1f ;
	 }
	
	public void processFloodFill(int[] m, int[] res, int nodeX, int nodeY, int target, int replacement, int stack, int width, int height){
		if (nodeX >= width || nodeY >= height || nodeX < 0 || nodeY < 0) return;
		//if (target == replacement) return;
		int b = m[nodeX + nodeY * width];
		if (isClearPixel(b)) return;
		if (res[nodeX + nodeY * width] == replacement) return;
		
		//if ((get(nodeX, nodeY) & Ship.BLOCK_ID_MASK) != target) return;
		//if (stack > 4000) return;
		//			3. Set the color of node to replacement-color.
		//set(nodeX, nodeY, replacement);
		res[nodeX + nodeY * width] = replacement;
		addNode(nodeX, nodeY-1);
		addNode(nodeX, nodeY+1);
		addNode(nodeX-1, nodeY);
		addNode(nodeX+1, nodeY);
		//			5. Return.

	}
	public void draw(SpriteBatch batch, OrthographicCamera camera, World world, Ship ship) {
		batch.setShader(null);
		batch.enableBlending();
		//batch.setProjectionMatrix(camera.combined);
		batch.setColor(Color.WHITE);
		batch.begin();
		if (region != null) batch.draw(region, 0, 0, ship.mapWidth, ship.mapHeight);
		batch.end();
		//batch.disableBlending();
	}

	public Pixmap getPixmap() {
		return mainPixmap;
	}

	public void dispose() {
		if (region != null){
			region.dispose();
		}
		region = null;
		
	}

	public void setTexture(Texture hull2) {
		if (region != null){
			region.dispose();
		}
		region = hull2;
		
	}

	public Texture getTexture() {
		// TODO Auto-generated method stub
		return region;
	}
}
