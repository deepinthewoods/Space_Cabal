package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Drone extends Ship {

    private static final String TAG = "drone";
    public transient Ship parent;
    private float zoom = .5f;
    public Drone(IntPixelMap map, Sprite pixelSprite, FontManager fonts, ShaderProgram shader) {
        super(map, pixelSprite, fonts, shader);
    }

    @Override
    public void updateCamera(OrthographicCamera wcamera, World world, int index) {
        super.updateCamera(wcamera, world, index);
        camera.zoom = parent.camera.zoom *zoom;
        camera.position.set(parent.camera.position)
                .add(-parent.mapWidth*.5f , -parent.mapHeight*.5f , 0)
        .scl(zoom)
                .add(offsetWorld.x, offsetWorld.y, 0)
                .add((mapWidth/2f), (mapHeight/2f), 0)
        //camera.position.set(parent.camera.position);//.add(-parent.mapWidth/2, -parent.mapHeight/2, 0);
        ;
        camera.update();
//        Gdx.app.log(TAG, "update camera " + (camera.zoom == parent.camera.zoom));
    }

}


