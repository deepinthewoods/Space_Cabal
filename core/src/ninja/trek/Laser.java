package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.ALaser;

public class Laser extends Entity {
	
	private static final String TAG = "laser";
	public int index;
	public int time;
	//public GridPoint2 target = new GridPoint2();
    public int weaponItemID;
    private ALaser alaser;

    public Laser() {
		glyph = " ";
		
	}
	@Override
	public Entity setDefaultAI() {
		resetAI();
		ActionList playerAction = new ActionList();
        alaser = Pools.obtain(ALaser.class);
		playerAction.addToStart(alaser);
		setAI(playerAction);
		return this;
	}
	transient Vector3 v = new Vector3();
	transient Vector2 mid = new Vector2();
	@Override
	public void draw(SpriteBatch batch, OrthographicCamera camera, World world) {
		float scale = camera.zoom;
		//Gdx.app.log(TAG, "djskljfl");
		Ship enemy = world.getEnemy(ship);
		if (enemy == null) return;
		Sprite spr = Sprites.laser;
		v.set(target.x, target.y, 0);
		enemy.camera.project(v);
		v.y = Gdx.graphics.getHeight()-1-v.y;
		//Gdx.app.log(TAG, "to " + v + target);
		camera.unproject(v);
		float w = mid.set(x, y).dst(v.x, v.y);
		spr.setPosition(x, y);
		spr.setSize(w, spr.getRegionHeight() * scale);
		spr.setRotation(mid.set(v.x, v.y).sub(x, y).angle());
		spr.draw(batch);
	}
	@Override
	public void reset() {
		time = 0;
		super.reset();
	}

    public void shoot() {
        alaser.shoot();
    }
}
