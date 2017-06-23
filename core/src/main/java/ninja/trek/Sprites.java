package ninja.trek;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Sprites {
public static Sprite laser;

public static void init(TextureAtlas atlas){
	laser = atlas.createSprite("laserbody");
}

public static void dispose(){
	
}
}
