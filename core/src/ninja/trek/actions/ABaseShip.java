package ninja.trek.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Ship;
import ninja.trek.entity.ShipEntity;
import ninja.trek.World;
import ninja.trek.action.Action;

public class ABaseShip extends Action{
	private static final String TAG = "base ship action";
	float shieldTimer = 0;
	public ABaseShip() {
		lanes = LANE_ACTING;
		//isBlocking = true;
	}
	@Override
	public void update(float dt, World world, Ship map, UI ui) {
		int power = 100;
		//Gdx.app.log(TAG, "update " + parent.e);

		AWaitForPath wait = Pools.obtain(AWaitForPath.class);
		Ship ship = parent.e.ship;
        ShipEntity shipE = ship.getShipEntity();
        if (shipE.shield < shipE.shieldTotal){
            shieldTimer += dt;
           // Gdx.app.log(TAG, "update " + shieldTimer + "  " + dt);
            float shieldTimeout = 5f;
            if (shieldTimer > shieldTimeout){
                shieldTimer = 0f;
                shipE.shield++;
            }
        }
        if (shipE.shieldTotal < shipE.shield)
            shipE.shield = shipE.shieldTotal;

		if (!ship.hasCategorizedBlocks) return;
		if (!ship.hasCalculatedConnectivity) ship.calculateConnectivity(world);
		int maxSystem = 0;
		for (int i = 0; i < ship.systemButtonOrder.length; i++){
			if (power == 0) break;
			maxSystem = ship.systemButtonOrder[i];
			int system = ship.systemButtonOrder[i];
			if (ship.disabledButton[system]) continue;
			Array<GridPoint2> blocks = ship.systemBlocks[system];
			int offset = MathUtils.random(blocks.size);
			for (int r = 0; r < blocks.size; r++){
				GridPoint2 pt = blocks.get((r + offset) % blocks.size);
				int block = ship.map.get(pt.x, pt.y);
				int depletion = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
				if (power == 0 ) break;
				if (depletion == 0) continue;
				int toRestore = Math.min(depletion, power);
				power -= toRestore;
				//if (toRestore != 0) Gdx.app.log(TAG, "restore " + toRestore + "  " + parent.e.ship);
				//else if (system == Ship.OXYGEN) Gdx.app.log(TAG, "oxy restore " + pt);
				depletion -= toRestore;
				block = block & (Ship.BLOCK_AIR_MASK | Ship.BLOCK_DAMAGE_MASK | Ship.BLOCK_FIRE_MASK | Ship.BLOCK_ID_MASK | Ship.BLOCK_BOOST_MASK);
				block |= depletion << Ship.BLOCK_DATA_BITS;
				ship.map.set(pt.x, pt.y, block);
				if (depletion == 0 && (block & Ship.BLOCK_DAMAGE_MASK) == 0)
					ship.map.needsBoost[block & Ship.BLOCK_ID_MASK].put(pt.x + pt.y * ship.mapWidth, 0);
			}
		}
		parent.e.ship.setSystemMarker(maxSystem);
		//Gdx.app.log(TAG, "last system " + Ship.systemNames[maxSystem]);

	}

	@Override
	public void onEnd(World world, Ship map) {
	}

	@Override
	public void onStart(World world, Ship map) {
	}

}
