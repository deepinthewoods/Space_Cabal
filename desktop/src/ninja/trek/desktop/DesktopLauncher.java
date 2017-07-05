package ninja.trek.desktop;

import com.badlogic.gdx.Files.FileType;
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

import ninja.trek.Main;

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static LwjglApplication createApplication() {
        return new LwjglApplication(new Main(){
        	public void makeFonts() {
        		String fontName = "kenpixel_high";
            	FileHandle fontFolder = Gdx.files.external(Main.FONT_SAVE_LOCATION);
        		fontFolder.mkdirs();
        		FileHandle fontFile = Gdx.files.external(Main.FONT_SAVE_LOCATION + fontName + ".fnt");
        		if (!fontFile.exists()){
        			BitmapFontWriter writer = new BitmapFontWriter();
            		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontName + ".ttf"));
            		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            		parameter.size = 32;
            		parameter.borderColor = Color.BLACK;
            		parameter.borderWidth = 2f;
            		parameter.packer = new PixmapPacker(512, 512, Format.RGBA8888, 2, false);
            		
            		BitmapFont font = generator.generateFont(parameter); // font size 12 pixels
            		
            		FontInfo info = new FontInfo();
            		info.padding = new Padding(1, 1, 1, 1);
            		info.outline = 2;
            		//writer.writeFont(font.getData(), font.g, fontFile, info);
            		writer.writeFont(font.getData(), new String[]{"ui.png"},
            				fontFile, info, 512, 512);
            		BitmapFontWriter.writePixmaps(parameter.packer.getPages(), fontFolder, "ui");
            		

            		generator.dispose(); // don't forget to dispose to avoid memory leaks!
        		}
        		
        	};
        }
        		
        		, getDefaultConfiguration());
    }

    private static LwjglApplicationConfiguration getDefaultConfiguration() {
    	
    	//TexturePacker.process("C:/Users/n/_spacecabal/sprites", "C:/Users/n/_spacecabal/assets", "background.atlas");
    	
    	
    	
    	
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