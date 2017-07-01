package ninja.trek;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

public class CustomColors {
private static final String TAG = "custom colors";
public static final Color NO_AIR_FLOOR = new Color(Color.DARK_GRAY);
private static final float VARIANCE = .25f;
public static 
Color 
  ENGINE = new Color(Color.RED)
, WALL = new Color(Color.LIGHT_GRAY)
, PHASER = new Color(Color.CYAN)
//, TORPEDO = new Color()
, DRONE = new Color(Color.CORAL)
, FLOOR = new Color(.25f, .25f, .25f, 1f)
, VACCUUM = new Color(Color.BLACK)
, OXYGEN = new Color(Color.LIME)
, SHIELD = new Color(0, .35f, 1, 1f)
, TELEPORTER = new Color(1, .35f, 1, 1f)
, SCIENCE = new Color(.25f, 1, .35f, 1f)

;
static HSL hc = new HSL();
public static Color[] color, lerpToColor;
public static String[] colorNames = {"vaccuum", "engine", "phaser", "shield", "wall", "floor", "oxygen", "drone", "teleporter", "science"};
private static float[] colorFloatArray, lerpFloatArray, flashFloatArray;
public static Color[] mapDrawColors = new Color[128]
		, mapLerpColors = new Color[16]
		, mapLerpColorsBoost = new Color[16]
		, mapFlashColors  = new Color[16]
		, mapFlashColorsB  = new Color[16]
		, mapFlashColorsFire  = new Color[16]
		, mapFlashColorsFireB  = new Color[16]
				;
private static float time;
private static Color[] lerpToColorBoost;;
public static void init(){
	//Pixmap pixmap = new Pixmap(Gdx.files.internal("palette.png"));
	
	NO_AIR_FLOOR.r += .3f;
	
	//Gdx.app.log("", "" + pixmap.getPixel(0, 0) + "  " + pixmap.getPixel(17, 2) + "\n"  );
	lerpToColor = new Color[16];
	for (int i = 0; i < lerpToColor.length; i++) lerpToColor[i] = new Color(Color.RED).lerp(Color.YELLOW, MathUtils.random(.8f));
	
	//Color.rgba8888ToColor(lerpToColor[Ship.ENGINE], pixmap.getPixel(36, 8));
	//Color.rgba8888ToColor(lerpToColor[Ship.SHIELD], pixmap.getPixel(52, 8));
	//Color.rgba8888ToColor(lerpToColor[Ship.PHASER], pixmap.getPixel(20, 8));
	//Color.rgba8888ToColor(lightColor[Ship.TORPEDO], pixmap.getPixel(4, 8));
	//Color.rgba8888ToColor(lerpToColor[Ship.OXYGEN], pixmap.getPixel(68, 8));
	
	WALL.set(1f, 1f, 1f, 1f);
	
	color = new Color[16];
	for (int i = 0; i < color.length; i++) color[i] = new Color(Color.WHITE);
	color[Ship.VACCUUM] = new Color(0f, 0f, 0f, 1f);
	color[Ship.ENGINE] = ENGINE;
	color[Ship.WEAPON] = PHASER;
	//color[Ship.TORPEDO] = TORPEDO;
	color[Ship.SHIELD] = SHIELD;
	color[Ship.WALL] = WALL;
	color[Ship.FLOOR] = FLOOR;
	color[Ship.OXYGEN] = OXYGEN;
	color[Ship.DRONE] = DRONE;
	color[Ship.TELEPORTER] = TELEPORTER;
	color[Ship.SCIENCE] = SCIENCE;
	for (int i = 0; i < color.length; i++){
		hc.fromRGB(color[i]);
		//
		if (i != Ship.FLOOR && i != Ship.WALL){
			//hc.s = 1f;
			//hc.l = .65f;
		}
		
		hc.toRGB(color[i]);
	}
	
	Colors.put("vaccuum", Color.WHITE);
	Colors.put("engine", color[Ship.ENGINE]);
	Colors.put("shield", color[Ship.SHIELD]);
	Colors.put("wall",  color[Ship.WALL]);
	Colors.put("phaser", color[Ship.WEAPON]);
	//Colors.put("torpedo", lightColor[Ship.TORPEDO]);
	Colors.put("floor", Color.WHITE);
	Colors.put("oxygen", color[Ship.OXYGEN]);
	Colors.put("drone", color[Ship.DRONE]);
	Colors.put("teleporter", color[Ship.TELEPORTER]);
	Colors.put("science", color[Ship.SCIENCE]);

	
	colorFloatArray = new float[512];
	lerpFloatArray = new float[color.length * 4];
	flashFloatArray = new float[color.length * 4];
	for (int i = 0; i < color.length; i++){
		colorFloatArray[i*4] = color[i].r;
		colorFloatArray[i*4+1] = color[i].g;
		colorFloatArray[i*4+2] = color[i].b;
		colorFloatArray[i*4+3] = 1f;
	}
	for (int i = 0; i < 128; i++){
		float index = (float)i / 128f + 1f/128f;
		mapDrawColors[i] = new Color(index, 1f, 1f, 1f);
	}
	
	lerpToColorBoost = new Color[16];
	for (int i = 0; i < lerpToColorBoost.length; i++) lerpToColorBoost[i] = new Color(color[i]).lerp(Color.WHITE, .35f);
	
	for (int i = 0; i < 16; i++){
		mapLerpColors[i] = new Color(1f, 1f, 1f, 1f);
		mapLerpColorsBoost[i] = new Color(1f, 1f, 1f, 1f);
		mapFlashColors[i] = new Color(1f, 1f, 1f, 1f);
		mapFlashColorsB[i] = new Color(1f, 1f, 1f, 1f);
		mapFlashColorsFire[i] = new Color(1f, 1f, 1f, 1f);
		mapFlashColorsFireB[i] = new Color(1f, 1f, 1f, 1f);
	}
}
public static float[] getFloatColorArray() {
	
	return colorFloatArray;
}

public static void updateColors(float dt, ShaderProgram shader){
	time += dt;
	int index = (int)((time % 1f) * 12);
	if (time > .5f){
		time = 0f;
		for (int i = 0; i < lerpToColor.length; i++) lerpToColor[i].set(Color.RED).lerp(Color.YELLOW, MathUtils.random(.2f, .8f));
	}
	float alpha = time * 4;
	if (alpha > 1f) alpha = 2f - alpha;
	alpha = alpha * alpha * (3-2*alpha);
	//Gdx.app.log(TAG, "alpha " + alpha);
	//if (time % 1f < .5f) alpha = 1f-alpha;
	
	for (int i = 0; i < 16; i++){
		mapLerpColors[i].set(color[i]);
		//if (alpha < .5f)
		mapLerpColors[i].lerp(lerpToColor[i], alpha);

		mapLerpColorsBoost[i].set(color[i]);
		mapLerpColorsBoost[i].lerp(lerpToColorBoost[i], alpha );
		
		hc.fromRGB(color[i]);
		float s1 = Math.min(hc.s + VARIANCE, 1f);
		float s0 = s1 - VARIANCE * 2;
		//s0 = 1;
		//s1 = 1;
		//hc.h = lerp(hc.h - .02f, hc.h + .02f, alpha);
		hc.s = lerp(.75f, 1, alpha);
		//hc.l = lerp(hc.l, hc.l - .2f, alpha);
		
		hc.toRGB(mapLerpColorsBoost[i]);
		//mapLerpColorsBoost[i].set(color[i]);
		
		mapFlashColorsFireB[i].set(mapLerpColors[i]);
		mapFlashColorsFire[i].set(mapLerpColors[i]);
		//mapLerpColors[i].set(Color.BLACK);
		mapFlashColors[i].set(color[i]).lerp(Color.BLACK, .42f);;
		mapFlashColorsB[i].set(color[i]).lerp(Color.BLACK, .42f);;
		if (index % 2 == 0){
			mapFlashColors[i].set(color[i]).lerp(Color.WHITE, .2f);
		} else {
			mapFlashColorsB[i].set(color[i]).lerp(Color.WHITE, .2f);			
		}
		if (index % 2 == 0){
			mapFlashColorsFire[i].set(mapFlashColorsB[i]);
		} else {
			mapFlashColorsFireB[i].set(mapFlashColors[i]);
		}
		
	}
	for (int ind = 0; ind < 16; ind++){
		int i = ind + 16;
		colorFloatArray[i*4] = mapLerpColors[ind].r;
		colorFloatArray[i*4+1] = mapLerpColors[ind].g;
		colorFloatArray[i*4+2] = mapLerpColors[ind].b;
		colorFloatArray[i*4+3] = 1f;
		i = ind + 32;
		colorFloatArray[i*4] = mapFlashColors[ind].r;
		colorFloatArray[i*4+1] = mapFlashColors[ind].g;
		colorFloatArray[i*4+2] = mapFlashColors[ind].b;
		colorFloatArray[i*4+3] = 1f;
		i = ind + 48;
		colorFloatArray[i*4] = mapFlashColorsB[ind].r;
		colorFloatArray[i*4+1] = mapFlashColorsB[ind].g;
		colorFloatArray[i*4+2] = mapFlashColorsB[ind].b;
		colorFloatArray[i*4+3] = 1f;
		
		i = ind + 64;
		colorFloatArray[i*4] = mapFlashColorsFire[ind].r;
		colorFloatArray[i*4+1] = mapFlashColorsFire[ind].g;
		colorFloatArray[i*4+2] = mapFlashColorsFire[ind].b;
		colorFloatArray[i*4+3] = 1f;
		
		i = ind + 80;
		colorFloatArray[i*4] = mapFlashColorsFireB[ind].r;
		colorFloatArray[i*4+1] = mapFlashColorsFireB[ind].g;
		colorFloatArray[i*4+2] = mapFlashColorsFireB[ind].b;
		colorFloatArray[i*4+3] = 1f;
		
		i = ind + 96;
		colorFloatArray[i*4] = mapLerpColorsBoost[ind].r;
		colorFloatArray[i*4+1] = mapLerpColorsBoost[ind].g;
		colorFloatArray[i*4+2] = mapLerpColorsBoost[ind].b;
		colorFloatArray[i*4+3] = 1f;
	}
	shader.begin();
	shader.setUniform4fv("u_colors[0]", colorFloatArray, 0, colorFloatArray.length);
	//shader.setUniform4fv("u_colors[0]", flashFloatArray, 128, flashFloatArray.length);

	shader.end();
}
private static float lerp(float a, float b, float t) {
    return (1-t)*a + t*b;
}

}
