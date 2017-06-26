package ninja.trek;

import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class EntityAI implements Poolable {
	protected Entity parent;
	public static String[] names = {"Shoot", "Fix", "Shield", "WAnder", "Engine", "Fire", "Weapon", "Power", "Oxygen"};
	
	public static final int SHOOT = 0;
	public static final int FIX = 1;
	public static final int SHIELDS = 2;
	public static final int WANDER = 3;
	public static final int ENGINE = 4;
	public static final int FIRE = 5;
	public static final int WEAPON = 6;
	public static final int POWER= 7;
	public static final int OXYGEN= 8;
	
	public EntityAI(){
		reset();
	}
	
	public void setParent(Entity e){
		parent = e;
	}

	public abstract void update(World world, Ship map);

	public void clear() {
		// TODO Auto-generated method stub
		
	}
		
}
