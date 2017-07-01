package ninja.trek;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.action.ActionList;
import ninja.trek.actions.AMissile;

public class Missile extends Entity {
	public Vector2 direction = new Vector2(), position = new Vector2();
	public float time;
	public Vector2 target = new Vector2();
	public int weeaponItemID;
	public Missile() {
		glyph = " ";
	}
@Override
public Entity setDefaultAI() {
	
	direction.set(1, 0);
	direction.rotate(MathUtils.random(-30, 30));
	ActionList playerAction = new ActionList();
	playerAction.addToStart(Pools.obtain(AMissile.class));
	setAI(playerAction);
	//setDefaultButtonOrder();
	return this;
}

@Override
public void draw(SpriteBatch batch, OrthographicCamera camera, World world) {
	WeaponItem weI = (WeaponItem) Items.getDef(weeaponItemID);
	Sprite sprite = (Sprite) Sprites.projectile[weI.variantIndex].getKeyFrame(ship.stateTime);
	batch.setProjectionMatrix(camera.combined);
	sprite.setCenterX(x);
	sprite.setCenterY(y);
	
	
	sprite.draw(batch);
}

}
