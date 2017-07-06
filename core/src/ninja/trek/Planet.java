package ninja.trek;

import com.badlogic.gdx.math.MathUtils;

public class Planet {

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
	public enum Type {INNER, EARTH_LIKE, MARS_LIKE, GAS_GIANT, METEOR, MOON};
	public Type planetType;
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
				landScale = .1f;
				height = MathUtils.random(.03f, .3f);
				oceanHeight = MathUtils.random(.56f, .7f);
				size = 1.2f;
				//oceanHeight = .56f;
				colorIndex = MathUtils.random(1);
				oceanColor = MathUtils.random(2)+1;
				beachAmount = .05f;
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
}
