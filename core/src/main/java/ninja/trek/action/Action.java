package ninja.trek.action;

import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

import ninja.trek.Ship;
import ninja.trek.World;


/**
 * Created by niz on 07/06/2014.
 */
public abstract class Action extends BinaryHeap.Node implements Poolable{
    public static final int LANE_DELAY = 1;
	public static final int LANE_ACTING = 2;
    public boolean isBlocking, isFinished;
    public int lanes;
    public transient ActionList parent;
    public boolean wasBlocked;
    public transient Action next; // Pointers to next and previous nodes
	protected transient Action prev;
	boolean first = true;
    
    public Action() {
        super(1f);
    }


    public abstract void update(float dt, World world, Ship map);

    public abstract void onEnd(World world, Ship map);

    public abstract void onStart(World world, Ship map);

    public Action addBeforeMe(Action a){
        parent.addBefore(this, a);
        return a;
    }
    public Action addAfterMe(Action a){
        parent.addAfter(this, a);
        return a;
    }
    
    public void addAfterMe(Class<Action> cl) {
		addAfterMe(Pools.obtain(cl));
	}

    public void addBeforeMe(Class<Action> cl) {
    	addBeforeMe(Pools.obtain(cl));
	}

    public void delay(float f){
        if (f == 0) return;
        //delayed = true;
        lanes |= LANE_DELAY;
        //removeSelf();
        float value = parent.currentTime+f;
        parent.delayedActions.add(this, value);
        //Gdx.app.log(TAG, "delay"+delayedActions.size);
        //Gdx.app.log(TAG, "delay"+ticks);
    }

    public void unDelay(){

        lanes &= ~LANE_DELAY;

    }
    
    
    
	  
    /** Returns the previous node of this node */
    public Action getPrev() { return prev; }
    /** Returns the next node of this node */
    public Action getNext() { return next; }
    
    /** Sets the previous node of this node */
    public void setPrev(Action newPrev) { prev = newPrev; }
    /** Sets the next node of this node */
    public void setNext(Action newNext) { next = newNext; }


	@Override
	public void reset() {
		first  = true;
		
	}

	

	
	  
	  
}
