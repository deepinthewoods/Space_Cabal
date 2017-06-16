package ninja.trek.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

public abstract class DragListenerSwap extends DragListener {
	private static final String TAG = "drag swap listener";
	protected int currentDragging = -1;
	public UI ui;
	private Table parentTable;
	private UIActionButton[] buttons;

	public DragListenerSwap(UI ui, Table parentTable, UIActionButton[] buttons) {
		this.ui = ui;
		this.parentTable = parentTable;
		this.buttons = buttons;
	}
	@Override
	public void dragStart(InputEvent event, float x, float y, int pointer) {
		//Gdx.app.log(TAG, "dragstart. local " + x + ", " + y);			
		Actor hit = parentTable.hit(x, y, true);
		if (hit != null) {
			//Gdx.app.log(TAG, "non button hit" + hit.getParent().getClass());
			Group par = hit.getParent();
			if (par != null && par instanceof UIActionButton){
				UIActionButton act = (UIActionButton) par;
				act.dragStart(x, y);
				currentDragging = act.index;
			}
		}
		super.dragStart(event, x, y, pointer);
	}
	@Override
	public void dragStop(InputEvent event, float x, float y, int pointer) {
		//Gdx.app.log(TAG, "dragstop. local " + x + ", " + y);	
		if (currentDragging != -1){
			buttons[currentDragging].dragStop(x, y);					
		}
		currentDragging = -1;
		super.dragStop(event, x, y, pointer);
	}
	@Override
	public void drag(InputEvent event, float x, float y, int pointer) {
		event.handle();
		//Gdx.app.log(TAG, "drag. local " + x + ", " + y);	
		Actor hit = parentTable.hit(x, y, true);
		if (hit != null) {
			//Gdx.app.log(TAG, "non button hit" + hit.getClass());
			Group par = hit.getParent();
			
			if (hit instanceof UIActionButton){
				//Gdx.app.log(TAG, "button hit" + hit.getClass());

				UIActionButton act = (UIActionButton) hit;
				if (act.index != currentDragging){
					swap(act.index, currentDragging);
					
				}
				event.handle();
			} else if (hit instanceof Label){
				if (par != null && par instanceof UIActionButton){
					UIActionButton act = (UIActionButton) par;
					if (act.index != currentDragging){
						swap(act.index, currentDragging);
					}
					event.handle();
				}
			}
		}
		super.drag(event, x, y, pointer);
		
	}

	public abstract void swap(int a, int b);
	void reset(){
		currentDragging = -1;
	}
}
