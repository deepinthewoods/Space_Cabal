package ninja.trek.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

import ninja.trek.CustomColors;
import ninja.trek.Ship;
import ninja.trek.World;

public class UISystemButton extends UIActionButton {

	private Skin skin;
	private UI ui;
	private World world;
	public UISystemButton(int index, Skin skin, Table parent, UI ui, World world) {
		super(index, Ship.systemNames[index], skin, parent);
		this.skin = skin;
		this.ui = ui;
		this.world = world;
		//this.getStyle().downFontColor = Color.GRAY;
		setStyle(skin.get("nonclickable", TextButtonStyle.class));
		this.getStyle().checkedFontColor = Color.GRAY;
		
		this.getStyle().font.getData().markupEnabled = true;
		this.setText("[" + CustomColors.colorNames[index] + "]" + getText());

	}
	
	@Override
	public void dragStop(float x, float y) {
		setText("[" + CustomColors.colorNames[index] + "]" + Ship.systemNames[index]);
	}
	@Override
	public void setChecked(boolean isChecked) {
		super.setChecked(isChecked);
		if (isChecked()){
			//this.setBackground(getStyle().checked);
			this.setColor(Color.RED);
		} else {
			this.setColor(Color.WHITE);
		}
	}
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (world.getPlayerShip().editMode){
			if (isChecked()){
				//this.setBackground(getStyle().checked);
				setColor(Color.RED);
			} else {
				setColor(Color.WHITE);
			}
			
		} else 	setColor(Color.WHITE);

		
		super.draw(batch, parentAlpha);
	}

	public void changeFont() {
		
	}
}
