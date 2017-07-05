package ninja.trek.ui;



import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ninja.trek.Entity;
import ninja.trek.Item;
import ninja.trek.Items;
import ninja.trek.Ship;
import ninja.trek.Weapon;
import ninja.trek.WeaponItem;

public class ItemDisplay extends Table {

	private static final int MAX_ITEMS = 10;
	public static final int MAX_WEAPONS = 10;
	private static final String TAG = "item displ w";
	private Table rightTable;
	private Table leftTable;
	private Table descTable;
	private Label descLabel;
	private Ship right;
	private Ship left;
	private ItemButton[] leftButtons;
	private ItemButton[] rightButtons;
	private Table actionsTable;
	public boolean showActionButtons = true;
	public TextButton[] weaponEquipButtons = new TextButton[MAX_WEAPONS];
	private Comparator<Weapon> weaponComparator;
	private ButtonGroup<ItemButton> itemButtonGroup;
	private TextButton closeButton;
	private Actor spacerActor;
	public ItemDisplay(Skin skin, final Window window){
		leftTable = new Table();
		add(leftTable).left();
		descTable = new Table();
		add(descTable).width(300);
		rightTable = new Table();
		add(rightTable);
		Label spacer = new Label("-------------------", skin);
		row();
		add(spacer);
		row();
		actionsTable = new Table();
		add(actionsTable).left().colspan(100);
		
		descLabel = new Label("", skin);
		descTable.add(descLabel);
		
		leftButtons = new ItemButton[MAX_ITEMS];
		rightButtons = new ItemButton[MAX_ITEMS];
		for (int i = 0; i < MAX_ITEMS; i++){
			final ItemButton buta = new ItemButton("", skin);
			leftButtons[i] = buta;
			leftButtons[i].addListener(new ChangeListener(){
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (!buta.isChecked()) return;
					showDescription(buta);
					if (showActionButtons){
						showActionButtons(buta, left);
						window.pack();
					}
					buta.setChecked(false);
				}
			});
			ItemButton but;
			but = new ItemButton("", skin);
			rightButtons[i] = but;
			rightButtons[i].addListener(new ClickListener(){
				
			});
		}
		
		weaponComparator = new Comparator<Weapon>(){
			@Override
			public int compare(Weapon a, Weapon b) {
				return a.index - b.index;
			}
		};
		itemButtonGroup = new ButtonGroup();
		for (int i = 0; i < weaponEquipButtons.length; i++){
			final TextButton but = new TextButton("Equip " + i, skin);
			final int index = i;
			weaponEquipButtons[i] = but;
			but.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					but.setChecked(false);
					ItemButton currentSelectedItem = itemButtonGroup.getChecked();
					left.equipWeapon(index, currentSelectedItem );
					super.clicked(event, x, y);
				}
			});
		}
		
		closeButton = new TextButton("close", skin);
		closeButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				closeButton.setChecked(false);
				window.remove();
				super.clicked(event, x, y);
			}
		});
		spacerActor = new Actor();
	}
	
	public void setLeft(Ship ship){
		Table table = leftTable;
		ItemButton[] buttons = leftButtons;
		left = ship;
		
		itemButtonGroup.clear();
		table.clearChildren();
		
		if (ship == null) return;
		for (int i = 0; i < ship.inventory.size; i++){
			//Item item = Items.getDef(ship.inventory.get(i));
			//Gdx.app.log(TAG, "add item button" + item + " " + ship.inventory.get(i));
			buttons[i].set(ship.inventory.get(i), i);
			table.add(buttons[i]).left();
			table.row();
			itemButtonGroup.add(buttons[i]);
		}
	}
	public void setRight(Ship ship){
		rightTable.clearChildren();
		right = ship;
		if (ship == null) return;
		
	}
	private void showDescription(ItemButton itemButton) {
		if (itemButton == null) throw new GdxRuntimeException("null button");
		//if (itemButton.item == null) throw new GdxRuntimeException("null item");
		Item item = Items.getDef(itemButton.itemID);
		descLabel.setText(item.description);
	}
	
	private Array<Weapon> weaps = new Array<Weapon>(true, 16);
	private void showActionButtons(ItemButton butt, Ship ship) {
		actionsTable.clear();
		Item item = Items.getDef(butt.itemID);
		if (item instanceof WeaponItem){
			weaps.clear();
			for (Entity e : ship.getEntities()){
				if (e instanceof Weapon){
					Gdx.app.log(TAG, "found weapon");
					Weapon w = (Weapon) e;
					weaps.add(w);
				}
			}
			weaps.sort(weaponComparator);
			for (int i = 0; i < weaps.size; i++){
				actionsTable.add(weaponEquipButtons[i]).left();
				Gdx.app.log(TAG, "add weapon equip btn");
			}
		}
		actionsTable.add(spacerActor).fillX().expandX().fill().right();
		actionsTable.add(closeButton).right();
	}
	public static class ItemButton extends TextButton{

		public int itemID;
		public int index;

		public ItemButton(String text, Skin skin) {
			super(text, skin);
		}

		public void set(int itemID, int index) {
			//if (item == null) throw new GdxRuntimeException("null item in set");
			this.itemID = itemID;
			this.index = index;
			Item item = Items.getDef(itemID);
			setText(item.name);
		}
		
	}
}
