package ninja.trek;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import squidpony.squidgrid.gui.gdx.SColor;

public class Planet {

	private static final String TAG = "planet";
	int seed;
	int index;
	public int parent = -1;
	public int parentOrder = -1;
	public int colorIndex;
	public float height = .01f;
	public float landScale = .15f;
	public float oceanHeight = .65f;
	public float mass = 1f;
	public float size = 1f;
	public float beachAmount = .01f;
	public int oceanColor;
	public int beachColor;
	public boolean exponentialHeightScaling = true;
	public enum Type {INNER, EARTH_LIKE, MARS_LIKE, GAS_GIANT, METEOR, MOON, STAR};
	public Type planetType;
	public IntArray quests = new IntArray();
	public IntArray completed = new IntArray();
	public Planet(int random, int index, Type type) {
		seed = random;
		this.index = index;
		planetType = type;
	}

	public String toString(){
		String s = "pPanet " + index;
		return s;
	}
	
	public void init(){
		MathUtils.random.setSeed(seed);
		
		//planetType = 1;
		switch (planetType){
		default:
		case INNER:
			landScale = .0f;
			height = .1f;
			size = .8f;
			oceanHeight = 0f;
			colorIndex = MathUtils.random(8, 11);
			break;
			
		case MARS_LIKE:
			
				landScale = .1f;
				height = MathUtils.random(.03f, .3f);
				oceanHeight = MathUtils.random(.1f, .4f);
				//oceanHeight = .4f;
				colorIndex = MathUtils.random(1)+2;
				oceanColor = MathUtils.random(1);
				//colorIndex = 2;
				break;
		case EARTH_LIKE:
				
				//earth
				landScale = 0f;
				height = MathUtils.random(.03f, .3f);
				oceanHeight = MathUtils.random(.56f, .7f);
				//oceanHeight = .2f;
				size = 1.2f;
				//oceanHeight = .56f;
				colorIndex = MathUtils.random(1);
				oceanColor = MathUtils.random(2)+1;
				beachColor = MathUtils.random(3);
				beachAmount = .01f;
				//colorIndex = 3;
				//oceanHeight = 0f;
				//height = .43f;
			
			break;
		case GAS_GIANT://gas giant
			landScale = .7f;
			oceanHeight = .1f;
			beachAmount = .2f;
			height = 0f;
			size = 1.5f;
			colorIndex = MathUtils.random(4, 7);
			break;
		case METEOR://meteor
			landScale = .1f;
			height = MathUtils.random(.15f, .9f);
			height = 1f;
			size = .8f;
			oceanHeight = .1f;
			beachAmount = .2f;
			colorIndex = MathUtils.random(4, 5);
			exponentialHeightScaling = false;
			break;
			
		case MOON://meteor
			landScale = .1f;
			height = MathUtils.random(0f, .2f);
			//height = 1f;
			size = .95f;
			oceanHeight = .1f;
			beachAmount = .2f;
			colorIndex = MathUtils.random(4, 5);
			exponentialHeightScaling = false;
			break;
		}
		
		
	}
	public static final Color[] OCEAN_COLORS = {SColor.BONDI_BLUE, SColor.CERULEAN, SColor.LIGHT_BLUE_SILK, SColor.CERULEAN_BLUE};
	private static final Color[] BEACH_COLORS = {SColor.YELLOW, SColor.TAN, SColor.LIGHT_GRAY, SColor.PEACH_YELLOW};

	Vector3 v = new Vector3();
	Color color, tc = new Color();
	static SimplexNoise noise = new SimplexNoise();
	public void makeTexture(FrameBuffer buffer, PlanetRenderer rend, SpriteBatch batch, Sprite pixelSprite) {
		buffer.begin();
		int w = buffer.getWidth();
		int h = buffer.getHeight();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
		batch.begin();
		float deltax = 360f / buffer.getWidth();
		float deltay = 180f / buffer.getHeight();
		for (int y = 0; y < buffer.getHeight(); y++) {
			
			v.set(0, 1, 0);
			v.rotate(deltay * y, 0, 0, 1);
			//Gdx.app.log(TAG, "x "  + " y " + y + " v" + v + "  = "  );
			for (int x = 0; x < buffer.getWidth(); x++) {
				v.rotate(deltax,  0, 1, 0);
				float sA = ( 1000 + seed ) % 10000f;
				float sB = ( 433433 + seed ) % 10000f;
				float sC = ( 4 + seed ) % 10000f;
				float temp = noise.scaled(v.x +sA, v.y * .5f + sB, v.z + sC, 5f)/2f+.5f;;
				temp = 1f - Math.abs(v.y) - temp * .15f;
				temp = Math.max(0, Math.min(1f,  temp));
				//if (Math.abs(v.y) < .1f) temp = 1f;
				float rainfall = noise.scaled(v.x+ 33333, v.y, v.z, 3f)/2f+.5f;
				float height = noise.scaled(v.x +sA, v.y * 1f + sB, v.z +sC, 1f)/2f+.5f;
	    		float oceanThreshold = oceanHeight ;
	    		//temp *= 1f - height;
	    		float adjHeight = Math.max(0, height - oceanThreshold);
	    		adjHeight *=  1f / (1f - oceanThreshold);
				//Gdx.app.log(TAG, "x " + x  + " adj " + adjHeight);
	    		//temp = 1f - adjHeight;
	    		temp *= temp;
				if (height > oceanThreshold)
					color = rend.lookupColor(1f-temp, 1f-rainfall, height, this);
				else if (height > oceanThreshold - beachAmount)
					color = BEACH_COLORS[beachColor];
				else color = OCEAN_COLORS[oceanColor];
				/*if (adjHeight > .5f) {
					color = tc;
					color.set(Color.MAGENTA);
					color.lerp(Color.PINK, adjHeight);
					//color = Color.MAGENTA;//
					temp = 0f;//1f - adjHeight * .5f;
					Gdx.app.log(TAG, "JKSDFSDKLJDSKL" + adjHeight);;
				}*/
				//if (temp < .5f)color = Color.WHITE;
				//else color = Color.RED;
				//color.set(Color.WHITE);
				//color.lerp(Color.BLACK, adjHeight);
				//if (adjHeight > .9f) color.set(Color.WHITE);
				drawPixel(x, y, color, batch, pixelSprite);
				
			}
		}
		batch.end();
		buffer.end();
		
	}

	private void drawPixel(int x, int y, Color color, SpriteBatch batch, Sprite pixelSprite) {
		pixelSprite.setColor(color);
		pixelSprite.setPosition(x, y);
		pixelSprite.draw(batch);
	}
	
}
