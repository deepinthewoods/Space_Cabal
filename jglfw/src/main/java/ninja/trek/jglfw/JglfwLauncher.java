package ninja.trek.jglfw;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;
import ninja.trek.Main;

/** Launches the desktop (JGLFW) application. */
public class JglfwLauncher {
    public static void main(final String[] args) {
        createApplication();
    }

    private static JglfwApplication createApplication() {
        return new JglfwApplication(new Main(), getDefaultConfiguration());
    }

    private static JglfwApplicationConfiguration getDefaultConfiguration() {
        final JglfwApplicationConfiguration configuration = new JglfwApplicationConfiguration();
        configuration.title = "SpaceCabal";
        configuration.width = 640;
        configuration.height = 480;
        return configuration;
    }
}