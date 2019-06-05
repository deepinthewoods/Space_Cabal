package ninja.trek;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

class ShipFactory {
    private final ShaderProgram shader;
    private final FontManager fontManager;
    private final Sprite pixelSprite;

    public ShipFactory(final Sprite pixelSprite, final FontManager fontManager, final ShaderProgram shader) {

        this.shader = shader;

        this.pixelSprite = pixelSprite;
        this.fontManager = fontManager;
        Pools.set(Drone.class, new Pool<Drone>(){

            @Override
            protected Drone newObject() {
                return new Drone(new IntPixelMap(64, 64), pixelSprite, fontManager, shader);
            }
        });
        Pools.set(Ship.class, new Pool<Ship>(){

            @Override
            protected Ship newObject() {
                return createShip();
            }
        });
    }

    public Ship createShip() {
        Ship ship = new Ship(new IntPixelMap(256, 256),  pixelSprite, fontManager, shader);
        return ship;
    }


}
