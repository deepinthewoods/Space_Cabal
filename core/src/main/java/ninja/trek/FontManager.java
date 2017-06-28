package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.GridPoint2;

public class FontManager {

	private BitmapFont[] fonts = new BitmapFont[4];
	private CharSequence spawnGlyph = "+";

	public FontManager(TextureAtlas atlas) {
		fonts[0] = new BitmapFont(Gdx.files.internal("ui/kenpixel_square-16.fnt"), atlas.findRegion("fonts"));
		fonts[1] = new BitmapFont(Gdx.files.internal("ui/kenpixel_future_square-16.fnt"), atlas.findRegion("fonts"));
		fonts[2] = new BitmapFont(Gdx.files.internal("ui/lunaboy-16.fnt"), atlas.findRegion("fonts"));
		fonts[3] = new BitmapFont(Gdx.files.internal("ui/romulus-16.fnt"), atlas.findRegion("fonts"));
		
		String fontName = "kenpixel_high";
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontName + ".ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 32;
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 2f;
		fonts[0] = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
		
	}

	public void draw(Entity e, SpriteBatch batch, OrthographicCamera cam) {
		BitmapFont font = fonts[e.font];
		float w2 = font.getData().getGlyph(e.glyph.charAt(0)).width * cam.zoom;
		font.draw(batch, e.glyph, e.x-w2, e.y + font.getCapHeight()/2);
		
		
		
	}

	public void setZoom(OrthographicCamera camera) {
		for (int i = 0; i < fonts.length; i++){
			fonts[i].getData().setScale(camera.zoom * 2);
			fonts[i].setUseIntegerPositions(false);
			
		}
	}

	public void drawSpawn(GridPoint2 e, SpriteBatch batch) {
		fonts[1].draw(batch, spawnGlyph , e.x, e.y + fonts[1].getCapHeight()/2);

	}

}
