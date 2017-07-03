package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;

public class FontManager {

	private BitmapFont[] fonts = new BitmapFont[4];
	private CharSequence spawnGlyph = "+";

	public FontManager() {
		TextureRegion reg = new TextureRegion(new Texture("ui/ui.png"));
		fonts[0] = new BitmapFont(Gdx.files.internal("ui/kenpixel_high_square-24.fnt"), reg);
		fonts[1] = new BitmapFont(Gdx.files.internal("ui/kenpixel_future-24.fnt"), reg);
		fonts[2] = new BitmapFont(Gdx.files.internal("ui/kenpixel_mini-24.fnt"), reg);
		fonts[3] = new BitmapFont(Gdx.files.internal("ui/kenpixel_blocks-24.fnt"), reg);
		
		TextureRegion regb = new TextureRegion(new Texture(Gdx.files.external(Main.FONT_SAVE_LOCATION + "ui.png")));

		fonts[0] = new BitmapFont(Gdx.files.external(Main.FONT_SAVE_LOCATION + "kenpixel_high.fnt"), regb);
		/*String fontName0= "kenpixel_high";
		String fontName1= "kenpixel_high";
		String fontName2= "kenpixel_high";
		String fontName3= "kenpixel_high";
		
		FileHandle fontFolder = Gdx.files.external(Main.FONT_SAVE_LOCATION);
		fontFolder.mkdirs();
		FileHandle fontFile0 = Gdx.files.external(Main.FONT_SAVE_LOCATION + fontName0 + ".fnt");
		FileHandle fontFile1 = Gdx.files.external(Main.FONT_SAVE_LOCATION + fontName1 + ".fnt");
		FileHandle fontFile2 = Gdx.files.external(Main.FONT_SAVE_LOCATION + fontName2 + ".fnt");
		FileHandle fontFile3 = Gdx.files.external(Main.FONT_SAVE_LOCATION + fontName3 + ".fnt");
		
		
		fonts[0] = new BitmapFont(fontFile0);
		fonts[1] = new BitmapFont(fontFile1);
		fonts[2] = new BitmapFont(fontFile2);
		fonts[3] = new BitmapFont(fontFile3);*/
		
		
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
		fonts[0].draw(batch, spawnGlyph , e.x, e.y + fonts[1].getCapHeight()/2);
		//Gdx.app.log("font man", "spawn " );

	}

	public void dispose() {
		for (int i = 0; i < fonts.length; i++){
			fonts[i].dispose();
		}
		
	}

}
