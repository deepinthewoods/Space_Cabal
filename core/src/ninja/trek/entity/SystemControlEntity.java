package ninja.trek.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ninja.trek.World;

public class SystemControlEntity extends Entity {
    public SystemControlEntity(){
        this.font = Entity.RACE_SYSTEM;
        setIconColor(Color.BLACK);
    }


    @Override
    public void drawBackground(SpriteBatch batch, OrthographicCamera camera, World world, Texture backgroundTexture) {

    }
}
