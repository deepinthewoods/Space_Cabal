package ninja.trek.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import ninja.trek.EntityAI;
import ninja.trek.entity.Entity;

public class UIActionButton extends TextButton{
	public int index;
	public Table parentTable;
	public float newXScale = 1;
	public UIActionButton(int index, Skin skin, Table parent) {
		//super(""+index, skin);
		this(index, Entity.jobNames[index],  skin, parent);
		setStyle(skin.get("nonclickable", TextButtonStyle.class));
		
	}

	public UIActionButton(int index, String string, Skin skin, Table parent) {
		super(string, skin);
		this.index = index;
		this.parentTable = parent;
		this.getStyle().checkedFontColor = Color.GRAY;

	}

	public void dragStart(float x, float y) {
		float width = getWidth();
		//setText("< >");
		setWidth(width);
		parentTable.getCell(this).width(width);
		
	}

	public void dragStop(float x, float y) {
		setText(Entity.jobNames[index]);
	}

	

}
