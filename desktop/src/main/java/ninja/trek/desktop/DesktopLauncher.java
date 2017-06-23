package ninja.trek.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import ninja.trek.Main;

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static LwjglApplication createApplication() {
        return new LwjglApplication(new Main(), getDefaultConfiguration());
    }

    private static LwjglApplicationConfiguration getDefaultConfiguration() {
    	
    	//TexturePacker.process("C:/Users/n/_spacecabal/sprites", "C:/Users/n/_spacecabal/assets", "background.atlas");
    	
    	
    	
    	
    	
        LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
        configuration.title = "SpaceCabal";
        configuration.width = 1024;
        configuration.height = 768;
        for (int size : new int[] { 128, 64, 32, 16 }) {
            configuration.addIcon("libgdx" + size + ".png", FileType.Internal);
        }
        return configuration;
    }
}