package ninja.trek.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import ninja.trek.Main;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(80 * 8, 40 * 18);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new Main();
        }
}