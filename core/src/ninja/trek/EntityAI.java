package ninja.trek;

import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Pool.Poolable;

import ninja.trek.entity.Entity;

public abstract class EntityAI implements Poolable {
	protected Entity parent;


	public EntityAI(){
		reset();
	}

	public void setParent(Entity e){
		parent = e;
	}

	public abstract void update(World world, Ship map, UI ui);

	public void clear() {
		// TODO Auto-generated method stub

	}
}
