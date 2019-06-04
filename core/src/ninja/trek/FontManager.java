package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.entity.Entity;

public class FontManager {

	private static final String TAG = "font manager";
	private BitmapFont[] fonts = new BitmapFont[5];
	private CharSequence spawnGlyph = "+";
	public static final String[] fontNames = {"kenpixel_high", "kenpixel_future_square", "kenpixel_mini", "kenpixel_blocks"};
	public static final int[] fontSizes = {32, 8, 8, 8};

	public FontManager(TextureAtlas atlas) {
		
		TextureRegion reg = new TextureRegion(new Texture("ui/ui.png"));
		//fonts[0] = new BitmapFont(Gdx.files.internal("ui/kenpixel_high_square-24.fnt"), reg);
		//fonts[1] = new BitmapFont(Gdx.files.internal("ui/kenpixel_future-24.fnt"), reg);
		////fonts[2] = new BitmapFont(Gdx.files.internal("ui/kenpixel_mini-24.fnt"), reg);
		//fonts[3] = new BitmapFont(Gdx.files.internal("ui/kenpixel_blocks-24.fnt"), reg);
		
		fonts[0] = makeFont(atlas, 0);
		fonts[1] = makeFont(atlas, 1);
		fonts[2] = makeFont(atlas, 2);
		fonts[3] = makeFont(atlas, 3);
		fonts[4] = makeFont(atlas, 0);
		//fonts[0] = new BitmapFont(data, regb, false);
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
		fonts[3] = new BitmapFont(fontFile3);//*/
		//fonts[0] = DefaultResources.getDefaultFont();
		//fonts[1] = DefaultResources.getDefaultFont();
		//fonts[2] = DefaultResources.getDefaultFont();
		//fonts[3] = DefaultResources.getDefaultFont();
		
	}

	private BitmapFont makeFont(TextureAtlas atlas, int index) {
		Array<AtlasRegion> regb = atlas.findRegions(fontNames[index]);
		Array<TextureRegion> pageRegions = new Array<TextureRegion>();
		for (int i = 0; i < regb.size; i++) {
			pageRegions.add(regb.get(i));
		}
		BitmapFontData data = new BitmapFont.BitmapFontData(Gdx.files.internal("ui/" +fontNames[index] +  ".fnt"), false);
		return new BitmapFont(data, pageRegions, false);
	}

	public void draw(Entity e, SpriteBatch batch, OrthographicCamera cam) {
		if (true) return;
		BitmapFont font = fonts[e.font];
		float w2 = font.getData().getGlyph(e.glyph.charAt(0)).width * cam.zoom;
		//e.glyphLayout =
		e.glyphLayout.setText(font, e.glyph);
		font.draw(batch, e.glyphLayout, e.x-e.glyphLayout.width/2, e.y + e.glyphLayout.height/2 );


		//GlyphLayout gly = Pools.obtain(GlyphLayout.class);

		//e.glyphLayout = gly;


		//Pools.free(gly);//TODO
		//Gdx.app.log(TAG, "draw " + e.glyphLayout.width + " " + e.glyph);
		
		
		
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

	public BitmapFont getFont(int font) {
		
		return fonts[font];
	}

}
