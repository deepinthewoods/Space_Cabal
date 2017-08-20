package ninja.trek.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.FontInfo;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.Padding;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import ninja.trek.FontManager;
import ninja.trek.MainSpaceCabal;

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static LwjglApplication createApplication() {
        return new LwjglApplication(new MainSpaceCabal(){
        	public void makeFonts() {
        		if (true) return;
        		//makeFont("kenpixel_high");
        		for (int i = 0; i < FontManager.fontNames.length; i++) {
        			makeFont(FontManager.fontNames[i], FontManager.fontSizes[i]);
        		}
        			
        		Settings settings = new Settings();
        		settings.maxWidth = 1024;
        		settings.maxHeight = 1024;
        		settings.paddingX = 0;
        		settings.paddingY = 0;
        		settings.duplicatePadding = false;
        		settings.edgePadding = false;
        		
        		
            	TexturePacker.process(settings, "C:/Users/n/Spacecabal/fonts", "C:/Users/n/_spacecabal/android/assets/ui", "ui.atlas");

            	FileHandle dest = Gdx.files.absolute("C:/Users/n/_spacecabal/android/assets/ui");
            	FileHandle fontFolder = Gdx.files.external(MainSpaceCabal.FONT_SAVE_LOCATION);
            	for (FileHandle f : fontFolder.list()) {
            		if (f.extension().contains("fnt")) {
            			Gdx.app.log("main", "copy file " + f.name() + dest.path());
            			f.copyTo(dest);
            		}
            	}

        		
        	}

			private void makeFont(String fontName, int fontSize) {
				Gdx.app.log("desktop main", "MAKE FONT");
				FileHandle fontFolder = Gdx.files.external(MainSpaceCabal.FONT_SAVE_LOCATION);
        		fontFolder.mkdirs();
        		FileHandle fontFile = Gdx.files.external(MainSpaceCabal.FONT_SAVE_LOCATION + fontName + ".fnt");
        		if (!fontFile.exists()){
        			BitmapFontWriter writer = new BitmapFontWriter();
            		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontName + ".ttf"));
            		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            		parameter.size = fontSize;
            		parameter.borderColor = Color.BLACK;
            		parameter.borderWidth = 2f;
            		parameter.packer = new PixmapPacker(256, 256, Format.RGBA8888, 2, false);
            		
            		BitmapFont font = generator.generateFont(parameter); // font size 12 pixels
            		
            		FontInfo info = new FontInfo();
            		info.padding = new Padding(1, 1, 1, 1);
            		info.outline = 2;
            		//writer.writeFont(font.getData(), font.g, fontFile, info);
            		writer.writeFont(font.getData(), new String[]{fontName + ".png"},
            				fontFile, info, 256, 256);
            		/*Array<Page> p = parameter.packer.getPages();
            		Pixmap[] pages = new Pixmap[p.size];
            		for (int i = 0; i < p.size; i++) {
            			pages[i] = p.get(i).getPixmap();
            		}
            		writer.writeFont(font.getData(), pages , fontFile, info);*/
            		
            		BitmapFontWriter.writePixmaps(parameter.packer.getPages(), fontFolder, fontName);
            	

            		generator.dispose(); // don't forget to dispose to avoid memory leaks!
        		}
				
			};
        }
        		
        		, getDefaultConfiguration());
    }

    private static LwjglApplicationConfiguration getDefaultConfiguration() {
    	
    	//TexturePacker.process("C:/Users/n/_spacecabal/sprites", "C:/Users/n/_spacecabal/assets", "background.atlas");
//    	TexturePacker.process("C:/Users/n/_spacecabal/android/assets/skins/holo/raw/dark-mdpi", "C:/Users/n/_spacecabal/android/assets/skins/holo/skin/dark-mdpi/", "Holo-dark-mdpi.atlas");
    	
    	
    	
    	
        LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
        configuration.title = "SpaceCabal";
        configuration.width = 1024;
        configuration.height = 768;
        for (int size : new int[] { 128, 64, 32, 16 }) {
            //configuration.addIcon("assets/Squid.png", FileType.Internal);
        }
        return configuration;
    }
}