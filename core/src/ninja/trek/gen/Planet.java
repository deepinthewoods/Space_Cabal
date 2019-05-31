package ninja.trek.gen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import ninja.trek.PlanetRenderer;
import ninja.trek.SimplexNoise;
import ninja.trek.items.LaserA;
import ninja.trek.items.RocketA;
import squidpony.squidgrid.gui.gdx.SColor;

public class Planet {

	private static final String TAG = "planet";
	public int seed;
	public int index;
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
	public int mountainColor = 0;
	public int mountainTopColor = 0;
	public boolean exponentialHeightScaling = true;
	public boolean hasMountains = false;



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
		if (parent == -1){
			String s = "Planet " + index;
			return s;

		} else {
			String s = "Moon " + index;
			return s;
		}
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
				size = 1.2f;
				landScale = MathUtils.random(0f, .2f);
				height = MathUtils.random(.03f, .13f);
				oceanHeight = MathUtils.random(.1f, .4f);
				//oceanHeight = .4f;
				colorIndex = MathUtils.random(1)+2;
				oceanColor = MathUtils.random(1);
				//colorIndex = 2;
				//mountainColor = MathUtils.random(3);
				//hasMountains = true;
				break;
		case EARTH_LIKE:
				
				//earth
				landScale = MathUtils.random(0f, .1f);
				height = MathUtils.random(.03f, .3f);
				oceanHeight = MathUtils.random(.56f, .7f);
				//oceanHeight = .2f;
				size = 1.2f;
				//oceanHeight = .56f;
				colorIndex = MathUtils.random(1);
				oceanColor = MathUtils.random(2)+1;
				beachColor = MathUtils.random(3);
				beachAmount = .012f;
				//colorIndex = 3;
				//oceanHeight = 0f;
				//height = .43f;
				hasMountains = true;
				mountainColor = MathUtils.random(3);
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
			size = .75f;
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
	private static final Color[] MOUNTAIN_COLORS = {SColor.LIGHT_GRAY, SColor.DB_GRAPHITE, SColor.LAVENDER_GRAY, SColor.SLATE_GRAY};
	private static final Color[] MOUNTAIN_TOP_COLORS = {SColor.WHITE, SColor.WHITE, SColor.WHITE, SColor.WHITE};

	Vector3 v = new Vector3();
	Color color, tc = new Color();
	static SimplexNoise noise = new SimplexNoise();
	public void makeTexture(FrameBuffer buffer, PlanetRenderer rend, SpriteBatch batch, Sprite pixelSprite) {
		color = tc;
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
				float rainfall = noise.scaled(v.x+ 33333, v.y +sA, v.z, 3f)/2f+.5f;
				float height = noise.scaled(v.x +sA, v.y * 1f + sB, v.z +sC, 10f * (landScale + .1f))/2f+.5f;
				float mountains = Math.abs(noise.scaled(v.x +sA + 100, v.y * 1f + sB + 80, v.z +sC, 1f));
				mountains = 1f - mountains;
				mountains = mountains * mountains;
				float mountainOverlay = noise.scaled(v.x +sA + 10, v.y * 1f + sB + 8, v.z +sC, 2f);
				if (mountainOverlay < 0f)
					mountains *= -mountainOverlay;
	    		float oceanThreshold = oceanHeight ;
	    		//temp *= 1f - height;
	    		float adjHeight = Math.max(0, height - oceanThreshold);
	    		adjHeight *=  1f / (1f - oceanThreshold);
				//Gdx.app.log(TAG, "x " + x  + " adj " + adjHeight);
	    		//temp = 1f - adjHeight;
	    		//temp *= temp;
				if (height > oceanThreshold) {
					color.set(rend.lookupColor(1f-temp, 1f-rainfall, height, this));
					if (hasMountains) {
						
						if (mountains > .9f) {
							float alpha = (mountains - .9f) * (1f/ .1f);
							
							Color mountainTopC = MOUNTAIN_TOP_COLORS[mountainTopColor];
							//if (MathUtils.randomBoolean())
							color.set(MOUNTAIN_COLORS[mountainColor]);
							if (MathUtils.random(3) == 0)
								color.lerp(mountainTopC, 1f);
						} else if (mountains > .75f) {
							float alpha = (mountains - .75f) * (1f/ .15f);
							
							Color mountainTopC = MOUNTAIN_COLORS[mountainTopColor];
							if (MathUtils.random() < alpha)
								color.lerp(mountainTopC, 1f);
						}
					}
				}
				else if (height > oceanThreshold - beachAmount)
					color.set(BEACH_COLORS[beachColor]);
				else color.set(OCEAN_COLORS[oceanColor]);
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
					//color.lerp(Color.BLACK, mountainOverlay);
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
