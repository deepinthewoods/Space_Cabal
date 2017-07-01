package ninja.trek;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Sprites {
private static final int PROJECTILES = 5;
public static Sprite laser;
public static Animation[] projectile;

public static void init(TextureAtlas atlas){
	laser = atlas.createSprite("laserbody");
	projectile = new Animation[PROJECTILES];
	for (int i = 0; i < PROJECTILES; i++)
		projectile[i] = new Animation(.2f, atlas.createSprites("projectile"+i));
}

public static void dispose(){
	
}
}
