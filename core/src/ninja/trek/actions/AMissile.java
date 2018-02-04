package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;

import ninja.trek.Items;
import ninja.trek.Missile;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.Action;
import ninja.trek.items.MissileItem;

public class AMissile extends Action {

	private static final float TIMEOUT = 1.5f;
	private static final String TAG = "missile action";

	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		//move
		Missile miss = (Missile) parent.e;
		//Gdx.app.log(TAG, "update missile " + miss.position);
		dt = World.timeStep;
		miss.time += dt;
		MissileItem weI = (MissileItem) Items.getDef(miss.weaponItemID);
		tmpV.set(miss.direction).scl(weI.moveSpeed * dt);
		
		miss.position.add(tmpV);
		miss.x = (int) miss.position.x;
		miss.y = (int) miss.position.y;
		if (parent.e.isHostile){
			tmpV.set(miss.position).sub(miss.target.x, miss.target.y);
			if (tmpV.x > 0 == miss.direction.x > 0){
				//Gdx.app.log(TAG, "DAMAGE" + miss.position);
				//TODO do damage
                miss.ship.missileDamage((int)miss.target.x, (int)miss.target.y, weI.damage);
				isFinished = true;
			}
			if (miss.time > TIMEOUT*10){
				isFinished = true;
			}
			tmpV.set(miss.ship.mapWidth/2, miss.ship.mapHeight/2);
			float dist2 = tmpV.dst2(miss.position);
			if (dist2 < miss.ship.shieldRadius2){

				//TODO damage shields / continue

                if (miss.ship.getShipEntity().shield > 0){
                    miss.ship.damageShield(weI.shieldDamage);
                    isFinished = true;
                    Gdx.app.log(TAG, "HIT SHIELDS");
                }
			}
			
		} else {//not hostile
			
			if (miss.time > TIMEOUT){
				miss.time = 0;
				parent.e.isHostile = true;
				world.switchToEnemyShip(miss);
				//Gdx.app.log(TAG, "SWITCH SHIPS");
				parent.e.isHostile = true;
				parent.e.x = (int) miss.target.x;
				parent.e.y = (int) miss.target.y;
				miss.direction.set(0, 1);
				miss.direction.rotate(MathUtils.random(-20, 20));
				if (MathUtils.randomBoolean()) miss.direction.scl(-1);
				tmpV.set(miss.direction).scl(-500);
				miss.x = (int) (miss.target.x + tmpV.x);
				miss.y = (int) (miss.target.y + tmpV.y);
				miss.position.set(miss.x, miss.y);
			}
		}
		//timeout

	}
	static Vector2 tmpV = new Vector2();
	@Override
	public void onEnd(World world, Ship map) {
		parent.e.ship.removeEntity(parent.e);
	}

	@Override
	public void onStart(World world, Ship map) {
		Missile miss = (Missile) parent.e;
		miss.time = 0f;
	}
	
}
