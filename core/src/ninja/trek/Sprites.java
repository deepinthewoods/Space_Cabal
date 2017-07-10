package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Sprites {
private static final int PROJECTILES = 5;
public static Sprite laser;
public static Animation[] projectile;
static Sprite[] sun;

public static void init(TextureAtlas atlas){
	laser = atlas.createSprite("laserbody");
	projectile = new Animation[PROJECTILES];
	for (int i = 0; i < PROJECTILES; i++) {
		projectile[i] = new Animation(.1f, atlas.createSprites("projectile"+i));
		//Gdx.app.log("sprites", "anim " + projectile[i].getKeyFrames().length);
	}
	sun = new Sprite[9];
	int ind = 0;
	sun[ind++] = atlas.createSprite("nova");
	sun[ind++] = atlas.createSprite("star");
	sun[ind++] = atlas.createSprite("aura");
	sun[ind++] = atlas.createSprite("divine");
	sun[ind++] = atlas.createSprite("iris");
	sun[ind++] = atlas.createSprite("pearlring");
	sun[ind++] = atlas.createSprite("pollen");
	sun[ind++] = atlas.createSprite("sparkle");
	sun[ind++] = atlas.createSprite("sun");
}

public static void dispose(){
	
}
}
