package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Drone extends Ship {

    private static final String TAG = "drone";
    public transient Ship parent;

    public Drone(IntPixelMap map, Sprite pixelSprite, FontManager fonts, ShaderProgram shader) {
        super(map, pixelSprite, fonts, shader);
    }

    @Override
    public void updateCamera(OrthographicCamera wcamera, World world, int index) {
        super.updateCamera(wcamera, world, index);
        camera.zoom = parent.camera.zoom;
        camera.position.set(parent.camera.position).add(offsetWorld.x, offsetWorld.y, 0)
                .add(-parent.mapWidth/2, -parent.mapHeight/2, 0).add(mapWidth/2, mapHeight/2, 0);
        //camera.position.set(parent.camera.position);//.add(-parent.mapWidth/2, -parent.mapHeight/2, 0);
        camera.update();
//        Gdx.app.log(TAG, "update camera " + (camera.zoom == parent.camera.zoom));
    }

}


