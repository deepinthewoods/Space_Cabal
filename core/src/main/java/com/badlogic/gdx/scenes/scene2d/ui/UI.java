package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import ninja.trek.Data;
import ninja.trek.Entity;
import ninja.trek.EntityAI;
import ninja.trek.IntPixelMap;
import ninja.trek.Main;
import ninja.trek.Ship;
import ninja.trek.Ship.EntityArray;
import ninja.trek.ShipEntity;
import ninja.trek.Weapon;
import ninja.trek.World;
import ninja.trek.ui.DragListenerSwap;
import ninja.trek.ui.ItemDisplay;
import ninja.trek.ui.UIActionButton;
import ninja.trek.ui.UISystemButton;

public class UI {

	protected static final String TAG = "UI";
	public static final String[] tileFileLocations = {"balls.png", "bluejunk.png", "chromey.png", "greyJunk.png", "greyShip.png", "redround.png"
			, "smallred.png", "smallships.png", "sourcegh.png", "weirdgrey.png", "white.png"};
	private static final int MAX_WEAPON_BUTTONS = 10;
	private DragAndDrop dnd;
	private UIActionButton[] buttons = new UIActionButton[EntityAI.names.length];
	private UISystemButton[] bottomButtons = new UISystemButton[Ship.systemNames.length];
	private Table entityActionTable;
	private TextButton dragLabel;
	Entity currentEntity;
	protected Vector2 v = new Vector2();
	private Table leftTable;
	private Table rightTable;
	private Table shipSystemTable;
	public ButtonGroup bottomGroup;
	public CheckBox xMirrorBtn;
	private Table editTable;
	public CheckBox destroyButton;
	public CheckBox fillBtn;
	public Slider brushSizeSlider;
	public CheckBox editLineButton;
	public Vector3 previewA = new Vector3(), previewB = new Vector3();
	public boolean previewLine;
	private LoadLabelPool loadLabelPool;
	private ButtonGroup loadGroup;
	private TextButton loadButtonInWindow;
	private TextButton loadCancelInWindow;
	public TextButton fireBtn;
	private CheckBox editButton;
	private ProgressBar infoLabel;
	private TextField fileNameInput;
	private Window overConfirmSaveWindow;
	private Label errorLabel;
	private Window saveFileWindow;
	private TextButton saveWButton;
	public Table table;
	private Actor topSpacerActor;
	private WeaponButton[] weaponButtons;
	private Table weaponTable;
	private Window invWindow;
	private ItemDisplay invItemDisplay;
	private Skin skin;
	float fontScale = 2f;
	public UI(Stage stage, World world) {
		String uiName = "holo"
				, fontName = "kenpixel_high"
				;
		//Skin skin = new Skin(Gdx.files.internal("skins/" + uiName + "/skin/" + uiName + "-ui.json"));
		//Skin skin = new Skin(Gdx.files.internal("skins/" + uiName + "/skin/skin.json"));
		skin = new Skin(Gdx.files.internal("skins/" + uiName + "/skin/dark-mdpi/Holo-dark-mdpi.json"));
		/*FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontName + ".ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 32;
		BitmapFont font = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!*/
		
		skin.get("default-font", BitmapFont.class).getData().setScale(fontScale);
		
		//skin.add("default-font", font, BitmapFont.class);
		
		loadLabelPool = new LoadLabelPool(skin);
		
		TextButtonStyle sty = skin.get(TextButton.TextButtonStyle.class);
		NinePatchDrawable uup = (NinePatchDrawable) sty.up;
		
		dragLabel = new TextButton(" ", skin);
		
		dnd = new DragAndDrop();
		table = new Table();
		entityActionTable = new Table();
		leftTable = new Table();
		rightTable = new Table();
		shipSystemTable = new Table();
		
		
		DragListener topDragL = new DragListenerSwap(this, entityActionTable, buttons){
			@Override
			public void swap(int a, int b) {
				ui.currentEntity.swap(a, b);
				ui.setEntity(ui.currentEntity);
			}};
		topDragL.setTapSquareSize(0f);
		entityActionTable.addListener(topDragL);
		entityActionTable.setTouchable(Touchable.enabled);
		for (int i = 0; i < EntityAI.names.length; i++){
			UIActionButton btn = new UIActionButton(i, skin, entityActionTable);
			buttons[i] = btn;
			entityActionTable.add(btn).left();
			btn.setTouchable(Touchable.enabled);
		}
		//actionTable.setBackground(skin.getDrawable("btn_default_pressed"));
		
		DragListener bottomDragL = new DragListenerSwap(this, shipSystemTable, bottomButtons){
			@Override
			public void swap(int a, int b) {
				Ship ship = world.getPlayerShip();
				ship.swap(a, b);
				ui.set(ship);
			}};
		bottomDragL.setTapSquareSize(0f);
		shipSystemTable.addListener(bottomDragL);
		shipSystemTable.setTouchable(Touchable.enabled);
		bottomGroup = new ButtonGroup();
		
		for (int i = 0; i < Ship.systemNames.length; i++){
			UISystemButton btn = new UISystemButton(i, skin, shipSystemTable, this, world);
			bottomButtons[i] = btn;
			shipSystemTable.add(btn).left();
			bottomGroup.add(btn);
			btn.changeFont();
			btn.setTouchable(Touchable.enabled);
		}
		
		/*Actor viewFillButton = new CheckBox("show fill", skin);
		viewFillButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TextButton btn = (TextButton) event.getListenerActor();
				world.getPlayerShip().drawFill = btn.isChecked();
				if (btn.isChecked()) world.getPlayerShip().map.setFloodFill(world.getPlayerShip().map);
				event.handle();
				super.clicked(event, x, y);
			}
		});*/
		//leftTable.add(viewFillButton);
		//leftTable.row();
		xMirrorBtn = new CheckBox("mirror", skin);
		editTable = new Table();
		final Table emptyEditTable = new Table();
		
		//editTable.row();
		fillBtn = new CheckBox("fill", skin);
		
		ButtonGroup editTableGroup = new ButtonGroup();
		editTableGroup.setMinCheckCount(0);
		editTableGroup.add(fillBtn);
		//editTableGroup.add(xMirrorBtn);
		
		//editTable.row();
		brushSizeSlider = new Slider(1f, 4f, 1f, false, skin){
			
		};
		Label brushSizeLabel = new Label("1", skin);

		brushSizeSlider.addListener(new ChangeListener(){

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				brushSizeLabel.setText("" + (int)brushSizeSlider.getValue());
			}
			
		});
		
		
		//editTable.row();
		editLineButton = new CheckBox("line", skin);
		
		editTableGroup.add(editLineButton);
		//editTable.row();
		ButtonGroup leftGroup = new ButtonGroup();
		leftGroup.setMinCheckCount(0);
		
		infoLabel =new ProgressBar(0f, 1f, 0.001f, false, skin){
			@Override
			public void draw(Batch batch, float parentAlpha) {
				//batch.setColor(Color.WHITE);
				ShipEntity se = ship.getShipEntity();
				if (se == null){
					return;
				}
				setValue(se.health / (float)se.totalHealth);
				
				super.draw(batch, 1f);
			}
		};
		//leftTable.row();
		/*
		 infoLabel =new Label("", skin){
			Vector3 v = new Vector3();
			@Override
			public void draw(Batch batch, float parentAlpha) {
				Ship ship = world.getPlayerShip();

				v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				ship.camera.unproject(v);
				int block = ship.map.get((int)v.x, (int)v.y);
				int air = (block & Ship.BLOCK_AIR_MASK) >> Ship.BLOCK_AIR_BITS;
				int dam = (block & Ship.BLOCK_DAMAGE_MASK) >> Ship.BLOCK_DAMAGE_BITS;
				int depl = (block & Ship.BLOCK_DATA_MASK) >> Ship.BLOCK_DATA_BITS;
				int fireI = (block & Ship.BLOCK_FIRE_MASK) >> Ship.BLOCK_FIRE_BITS;
				boolean fire = fireI > 0;
				int fires = 0;
				bits.clear();
				bits.or(ship.map.onFire);
				int fromIndex = 0;
				while (bits.nextSetBit(fromIndex) != -1){
					fromIndex = bits.nextSetBit(fromIndex+1);
					fires++;
					
				}
				
				setText("air:"+air + " depl " + depl + " f" + fire + "\ndam:" + dam  + " fps" + Gdx.graphics.getFramesPerSecond()
				+ "\nfires:" + fires);
				
				super.draw(batch, parentAlpha);
			}
		};
		  
		 */
		
		
		
		//leftTable.add(editButton).left();
		
		destroyButton = new CheckBox("destroy", skin);
		destroyButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
			}
		});
		
		editTableGroup.add(destroyButton);
		
		fireBtn = new CheckBox("fire", skin);
		
		editTableGroup.add(fireBtn);
		
		
		TextButton weaponButton = new TextButton("add gun", skin);
		weaponButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				weaponButton.setChecked(false);
				Ship ship = world.getPlayerShip();
				ship.placeWeapon = true;
				ship.deleteWeapon = false;
				ship.placeSpawn = false;
				super.clicked(event, x, y);
			}
		});
		
		TextButton weaponDeleteButton = new TextButton("del gun", skin);
		weaponDeleteButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				weaponDeleteButton.setChecked(false);
				Ship ship = world.getPlayerShip();
				ship.deleteWeapon = true;
				ship.placeWeapon = false;
				ship.placeSpawn = false;
				super.clicked(event, x, y);
			}
		});
		
		
		TextButton entitySpawnButton = new TextButton("spawn", skin);
		entitySpawnButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entitySpawnButton.setChecked(false);
				Ship ship = world.getPlayerShip();
				ship.deleteWeapon = false;
				ship.placeWeapon = false;
				ship.placeSpawn = true;
				
				super.clicked(event, x, y);
			}
		});
		
		
		
		Window hullWindow = new Window("Generate Outer Hull Image", skin);
		Slider hullRepsSlider = new Slider(1, 6, 1, false, skin);
		Slider hullRadiusSlider = new Slider(1, 6, 1, false, skin);
		
		TextButton openHullButton = new TextButton("hull", skin);
		openHullButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				openHullButton.setChecked(false);
				stage.addActor(hullWindow);
				super.clicked(event, x, y);
			}
		});
		
		ButtonGroup hullGroup = new ButtonGroup();
		FileHandle[] tileFiles = Gdx.files.absolute(Gdx.files.internal("sources/").file().getAbsolutePath()).list();
		for (String path : tileFileLocations){
			TextButton b = new TextButton("" + path, skin);
			hullGroup.add(b);
			hullWindow.add(b).colspan(2);
			hullWindow.row();
		}
		TextButton hullCalcButton = new TextButton("Generate", skin);
		hullCalcButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hullCalcButton.setChecked(false);
				Ship ship = world.getPlayerShip();
				CharSequence name = ((TextButton)hullGroup.getChecked()).getText();
				int reps = (int) hullRepsSlider.getValue();
				int radius = (int) hullRadiusSlider.getValue();
				ship.hull.calculate(ship, name.toString(), reps, radius);
				super.clicked(event, x, y);
			}
		});
		
		TextButton hullCloseButton = new TextButton("Close", skin);
		hullCloseButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hullCloseButton.setChecked(false);
				hullWindow.remove();
				super.clicked(event, x, y);
			}
		});
		hullWindow.row();
		Label hullRepsLabel = new Label("reps(1)", skin);
		hullWindow.add(hullRepsLabel);
		hullWindow.add(hullRepsSlider);
		hullRepsSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullRepsLabel.setText("reps(" + (int)hullRepsSlider.getValue() + ")");
			}
		});
		hullWindow.row();
		Label hullRadiusLabel = new Label("radius(1)", skin);
		hullWindow.add(hullRadiusLabel);
		hullWindow.add(hullRadiusSlider);
		hullRadiusSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullRadiusLabel.setText("radius(" + (int)hullRadiusSlider.getValue() + ")");
			}
		});
		hullWindow.row();
		hullWindow.add(hullCalcButton);
		hullWindow.add(hullCloseButton);
		hullWindow.pack();
		
		//editTable.row();/////////////////////////////////////////////////////////////////////////
		
		
		saveWButton = new TextButton("save", skin);

		//editTable.row();
		saveFileWindow = new Window("Enter Ship Name", skin);
		fileNameInput = new TextField("", skin){
		    @Override
		    protected InputListener createInputListener () {
		        return new TextFieldClickListener(){
		            @Override
		            public boolean keyUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
		            	if (keycode == Keys.ENTER){
		            		//saveWButton.toggle();
		            		//saveWButton.getClickListener().clicked(null, 0, 0);
		    				saveGameFromClick(world, stage);

		            	}
		                //System.out.println("event="+event+" key="+keycode);
		                return super.keyUp(event, keycode);
		            };
		        };
		    }
		};
		TextButton overYes = new TextButton("Yes", skin);
		overConfirmSaveWindow = new Window("File Exists. Overwrite?", skin);
		overConfirmSaveWindow.addListener(new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				overYes.setChecked(false);
				overConfirmSaveWindow.remove();
				saveFileWindow.remove();
				String name = fileNameInput.getText();
				Ship ship = world.getPlayerShip();

				saveShip(name, ship);
				return true;
			}
		});
			
		
		overConfirmSaveWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		TextButton overNo = new TextButton("No", skin);
		
		overNo.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				overNo.setChecked(false);
				overConfirmSaveWindow.remove();
				super.clicked(event, x, y);
			}
		});
		overYes.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				overYes.setChecked(false);
				overConfirmSaveWindow.remove();
				saveFileWindow.remove();
				String name = fileNameInput.getText();
				Ship ship = world.getPlayerShip();

				saveShip(name, ship);
				super.clicked(event, x, y);
			}

			
		});
		overConfirmSaveWindow.add(overYes);
		overConfirmSaveWindow.add(overNo);
		overConfirmSaveWindow.pack();
		//saveFileWindow.setColor(Color.DARK_GRAY);
		
		saveFileWindow.add(fileNameInput);
		saveFileWindow.row();
		errorLabel = new Label("", skin);
		saveFileWindow.add(errorLabel);
		saveFileWindow.row();
		TextButton closeButton = new TextButton("cancel", skin);
		closeButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				closeButton.setChecked(false);
				saveFileWindow.remove();
				super.clicked(event, x, y);
			}
		});
		saveFileWindow.add(closeButton);
		saveWButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveGameFromClick(world, stage);
				super.clicked(event, x, y);
			}

			

			

			
		});
		saveFileWindow.add(saveWButton);
		
		saveFileWindow.pack();
		saveFileWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		
		TextButton saveButton = new TextButton("Save", skin);
		saveButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveButton.setChecked(false);
				errorLabel.setText("");
				stage.addActor(saveFileWindow);
				//fileNameInput;
				stage.setKeyboardFocus(fileNameInput);
				super.clicked(event, x, y);
			}
		});
		
		Window loadWindow = new Window("Select Ship to Load", skin);
		loadWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);

		loadGroup = new ButtonGroup();
		
		loadButtonInWindow = new TextButton("Load Selected", skin);
		loadButtonInWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);

		loadButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Ship ship = world.getPlayerShip();
				LoadLabel load = (LoadLabel) loadGroup.getChecked();
				load.loadFileInto(ship);
				loadWindow.remove();
				loadButtonInWindow.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		loadCancelInWindow = new TextButton("Cancel", skin);
		

		loadCancelInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				loadCancelInWindow.setChecked(false);
				loadWindow.remove();
				super.clicked(event, x, y);
			}
		});
		
		TextButton loadButton = new TextButton("Load", skin);
		loadButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				populateLoadWindow(loadWindow);
				stage.addActor(loadWindow);
				loadButton.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		
		TextButton startButton = new TextButton("Start", skin);
		startButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//populateLoadWindow(loadWindow);
				//stage.addActor(loadWindow);
				startButton.setChecked(false);
				world.startTestBattle();
				set(world.getPlayerShip());
				super.clicked(event, x, y);
			}
		});
		
		Window newShipWindow = new Window("Select Settings for New Ship", skin);
		newShipWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		Slider xSlider = new Slider(1, 8, 1, false, skin);
		Label xSliderLabel = new Label("x " + Main.CHUNK_SIZE , skin);
		Slider ySlider = new Slider(1, 8, 1, false, skin);
		Label ySliderLabel = new Label("y " + Main.CHUNK_SIZE, skin);
		xSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				xSliderLabel.setText("x " + ((int)xSlider.getValue() * Main.CHUNK_SIZE));
			}
		});
		ySlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ySliderLabel.setText("y " + ((int)ySlider.getValue() * Main.CHUNK_SIZE));
			}
		});
		newShipWindow.add(xSliderLabel);
		newShipWindow.add(xSlider);
		newShipWindow.row();
		newShipWindow.add(ySliderLabel);
		newShipWindow.add(ySlider);
		TextButton createShipButtonInWindow = new TextButton("Create", skin);
		createShipButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				createShipButtonInWindow.setChecked(false);
				newShipWindow.remove();
				int w = (int)xSlider.getValue() * Main.CHUNK_SIZE;
				int h = (int)ySlider.getValue() * Main.CHUNK_SIZE;
				
				EntityArray entities = Pools.obtain(EntityArray.class);
				world.getPlayerShip().load(new IntPixelMap(w, h, Main.CHUNK_SIZE), entities );
				super.clicked(event, x, y);
			}
		});
		TextButton cancelCreateShipButtonInWindow = new TextButton("cancel", skin);
		cancelCreateShipButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				cancelCreateShipButtonInWindow.setChecked(false);
				newShipWindow.remove();
				super.clicked(event, x, y);
			}
		});
		newShipWindow.row();
		newShipWindow.add(createShipButtonInWindow);
		newShipWindow.add(cancelCreateShipButtonInWindow);
		newShipWindow.pack();
		
		TextButton newShipButton = new TextButton("New", skin);
		newShipButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//populateLoadWindow(loadWindow);
				stage.addActor(newShipWindow);
				
				super.clicked(event, x, y);
			}
		});
		
		editTable.add(saveButton);
		editTable.row();
		editTable.add(loadButton);
		editTable.row();
		editTable.add(startButton);
		editTable.row();
		editTable.add(newShipButton);
		editTable.row();
		editTable.add(brushSizeSlider).colspan(2);
		//editTable.row();
		editTable.add(brushSizeLabel);
		editTable.row();
		editTable.add(xMirrorBtn).left();
		editTable.row();
		editTable.add(fillBtn).left();
		editTable.row();
		editTable.add(editLineButton).left();
		editTable.row();
		editTable.add(weaponButton);
		editTable.row();
		editTable.add(weaponDeleteButton);
		editTable.row();
		editTable.add(entitySpawnButton);
		editTable.row();
		editTable.add(destroyButton).left();
		editTable.row();
		editTable.add(fireBtn);
		editTable.row();
		editTable.add(openHullButton);
		editTable.row();
		
		ScrollPaneN editPane = new ScrollPaneN(emptyEditTable);
		editPane.setOverscroll(false, true);
		
		//leftTable.add(infoLabel).left();
		//leftTable.add(new Label("", skin)).expandX();
		editButton = new CheckBox("edit", skin);
		editButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton btn = (TextButton) event.getListenerActor();
				world.getPlayerShip().editMode = btn.isChecked();
				event.handle();
				if (btn.isChecked()){
					emptyEditTable.add(editTable);
				} else {
					editTable.remove();
				}
				set(ship);
				table.invalidate();
				stage.setScrollFocus(editPane);
			}
			
		});
		
		leftTable.row();
		leftTable.add(editButton).left();
		
		leftTable.row();
		
		leftTable.add(editPane);
		leftGroup.add(editButton);
		leftTable.row();
		
		weaponButtons = new WeaponButton[MAX_WEAPON_BUTTONS];
		weaponTable = new Table();
		for (int i = 0; i < MAX_WEAPON_BUTTONS; i++){
			final int index = i;
			weaponButtons[i] = new WeaponButton(skin);
			weaponButtons[i].addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					weaponButtons[index].setChecked(false);
					world.targettingIndex = index;
					world.getPlayerShip().cancelWeaponTarget(index);
					super.clicked(event, x, y);
				}
			});
		}
		Table topTable = new Table();
		table.setFillParent(true);
		table.add(topTable).top().left();
		topTable.add(infoLabel).top().left();
		topTable.add(shipSystemTable);
		
		//table.row();
		table.row();
		//table.add(new Actor()).expand();
		//table.row();
		
		table.add(leftTable).left().top();
		table.add(new Actor()).expand();
		table.add(rightTable);
		table.row();
		//table.add(bottomWeaponTable).left();
		table.add(weaponTable).colspan(10).left();

		table.row();
		table.add(entityActionTable).left();
		
		table.setTouchable(Touchable.enabled);
		stage.addActor(table);
		//stage.addActor(dragLabel);
		table.layout();
		
		topSpacerActor = new Actor();
		
		makeInventoryWindow(skin);
		
		

	}
	Bits bits = new Bits();
	private Ship ship;
	private Entity entity;

	private void makeInventoryWindow(Skin skin) {
		invWindow = new Window("Inventory", skin);
		invItemDisplay = new ItemDisplay(skin, invWindow);
		invWindow.add(invItemDisplay);
	}

	public void openInventory(Ship playerShip) {
		invItemDisplay.setRight(null);
		invItemDisplay.setLeft(playerShip);
		invWindow.pack();
		invWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		table.addActor(invWindow);		
		//Gdx.app.log(TAG, "open inv");
	}

	public void set(Ship ship) {
		this.ship = ship;
		shipSystemTable.clearChildren();
		//bottomTable.row();
		if (ship != null){
			
			for (int i = 0; i < ship.systemButtonOrder.length; i++){
				if (ship.editMode || (ship.systemButtonOrder[i] != Ship.VACCUUM && ship.systemButtonOrder[i] != Ship.FLOOR && ship.systemButtonOrder[i] != Ship.WALL)){
					shipSystemTable.add(bottomButtons[ship.systemButtonOrder[i]]);
					
				}
			}
			//currentEntity = e;
			shipSystemTable.invalidate();
			weaponTable.clearChildren();
			float maxW = 0, total = 0;
			for (int i = 0; i < bottomButtons.length; i++){
				if (ship.editMode || (i != Ship.VACCUUM && i != Ship.FLOOR && i != Ship.WALL))
				{
					float w = bottomButtons[i].getWidth();
					maxW += w;
					total++;
				}
			}
			maxW = (int) (( Gdx.graphics.getWidth() - infoLabel.getWidth() ) / total);
			
			for (int i = 0; i < bottomButtons.length; i++){
				if (ship.editMode || (ship.systemButtonOrder[i] != Ship.VACCUUM && ship.systemButtonOrder[i] != Ship.FLOOR && ship.systemButtonOrder[i] != Ship.WALL)){
					if (shipSystemTable.getCell(bottomButtons[ship.systemButtonOrder[i]]) == null) throw new GdxRuntimeException("null " + i);
					shipSystemTable.getCell(bottomButtons[ship.systemButtonOrder[i]]).width(maxW).pad(0).space(0);
				}
			}
			int i = 0;
			for (Entity e : ship.getEntities()){
				//Gdx.app.log(TAG, "look at e " + e.getClass());
				if (e instanceof Weapon){
					Weapon w = (Weapon) e;
					//ship.equippedWeapons[w.index];
					//Gdx.app.log(TAG, "ADDDD WEAPONNNNNNNNNNNNN");
					TextButton b = weaponButtons[i];
					b.setText("weapon " + i);
					weaponTable.add(b);
					
					i++;
				}
			}
			weaponTable.row();
			weaponTable.layout();
		}else {
			shipSystemTable.add(topSpacerActor).expandX();
		}
		
		
	}

	public void setEntity(Entity e){
		entity = e;
		Table actionTable = this.entityActionTable;
		UIActionButton[] buttons2 = buttons;
		actionTable.clearChildren();
		if (e != null){
			for (int i = 0; i < e.buttonOrder.length; i++){
				buttons2[i].getLabel().setFontScale(fontScale);
				actionTable.add(buttons2[e.buttonOrder[i]]);
			}
			currentEntity = e;
			
			int maxW, total = 0;
			//float scale = skin.get("default-font", BitmapFont.class).getData().scaleX;
			//while (tooBig && scale > .3f)
			{
				maxW = 0;
				total = 0;
				
				for (int i = 0; i < buttons2.length; i++){
					{
						float w = buttons2[i].getWidth();
						//if (buttons[i].getWidth() <= buttons[i].getMinWidth()+1)
						
						maxW += w;
						total++;
					}
				}
				
				
			}
			maxW = (int) (( Gdx.graphics.getWidth()  ) / total);
			actionTable.layout();
			table.layout();
			
			
			for (int i = 0; i < buttons2.length; i++){
				actionTable.getCell(buttons2[i]).width(maxW).pad(0).space(0);
				//buttons[i].getCell(buttons[i].getLabel()).fill();
				//if (buttons[i].getLabel().getWidth() >= buttons[i].getWidth() - 3)
				float sc = (maxW / buttons2[i].getLabel().getWidth()) * .8f;
				sc = Math.min(1f, sc);
				sc *= fontScale;
				buttons2[i].getLabel().setFontScaleX(sc);
			}
		} 
		
		
		actionTable.invalidate();
		//table.invalidate();
	}
	
	
	public void reset(){

	}

	public void setPreviewLine(Vector3 v, Vector3 v2) {
		previewA .set(v);
		previewB.set(v2);
	}
	private boolean fileExists(String name) {
		FileHandle file = Gdx.files.external(Main.SHIP_SAVE_LOCATION + name + "." + Main.MAP_FILE_EXTENSION);
		if (file.exists()){
			return true;
		}
		return false;
	}
	private boolean isValidFileName(String name, Label errorLabel) {
		if (name.contains("/") || name.matches("[\\.\\\\]")){
			errorLabel.setText("invalid file name");
			return false;
		};
		return true;
	}
	
	private void populateLoadWindow(Window loadWindow) {
		for (Actor c : loadWindow.getChildren()){
			if (c instanceof LoadLabel) loadLabelPool.free((LoadLabel) c);
		}
		loadWindow.clearChildren();
		loadGroup.clear();
		FileHandle folder = Gdx.files.external(Main.SHIP_SAVE_LOCATION);
		FileHandle[] list = folder.list(Main.MAP_FILE_EXTENSION);
		for (FileHandle f : list){
			LoadLabel loadLabel = loadLabelPool.obtain();
			loadLabel.set(f);
			loadWindow.add(loadLabel);
			loadGroup.add(loadLabel);
			loadWindow.row();
		}
		loadWindow.add(loadButtonInWindow);
		loadWindow.add(loadCancelInWindow);
		loadWindow.pack();
	}

	public void saveShip(String name, Ship ship) {
		//Gdx.app.log(TAG, "actually save " + name + Main.MAP_FILE_EXTENSION);
		FileHandle file = Gdx.files.external(Main.SHIP_SAVE_LOCATION + name + "." + Main.MAP_FILE_EXTENSION);
		FileHandle entityFile = Gdx.files.external(Main.SHIP_SAVE_LOCATION + name + "." + Main.ENTITY_FILE_EXTENSION);
		Json json = new Json();
		String string = json.toJson(ship.map);
		String entities = json.toJson(ship.getEntities());
		file.writeString(string, false);
		entityFile.writeString(entities, false);
		//savePreview(name, ship);
	}
	
	private void savePreview(String name, Ship ship) {
		FileHandle file = Gdx.files.external(Main.SHIP_SAVE_LOCATION + name + "." + Main.MAP_PREVIEW_EXTENSION);

		ship.savePreview(file, ship);
	}
	private void saveGameFromClick(World world, Stage stage) {
		String name = fileNameInput.getText();
		if (fileExists(name)){
			stage.addActor(overConfirmSaveWindow);
			;
			stage.setKeyboardFocus(overConfirmSaveWindow);
		} else if (isValidFileName(name, errorLabel)){
			
			Ship ship = world.getPlayerShip();
			saveShip(name, ship);
			saveFileWindow.remove();
		} 
		saveWButton.setChecked(false);
		
	}
	
	public void resize(){
		table.invalidate();
		set(ship);
		setEntity(entity);
	}

	private static class LoadLabelPool extends Pool<LoadLabel>{
		private Skin skin;
		public LoadLabelPool(Skin skin) {
			this.skin = skin;
		}
		@Override
		protected LoadLabel newObject() {
			LoadLabel label = new LoadLabel(skin);
			label.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					
					//label.setChecked(false);
					super.clicked(event, x, y);
				}
			});
			return label;
		}
		
	}
	private static class LoadLabel extends TextButton{

		private FileHandle f;

		public LoadLabel(Skin skin) {
			super("", skin);
			
		}

		public void loadFileInto(Ship ship) {
			Json json = Data.jsonPool.obtain();
			IntPixelMap map = json.fromJson(IntPixelMap.class, f.readString());
			FileHandle entityFile = Gdx.files.external(f.pathWithoutExtension() + "." + Main.ENTITY_FILE_EXTENSION);
			EntityArray entities = json.fromJson(EntityArray.class, entityFile.readString());
			ship.load(map, entities);
			Data.jsonPool.free(json);
		}

		public void set(FileHandle f) {
			this.f = f;
			setText(f.name());
			//setChecked(false);
		}
		
	}
	private static class WeaponButton extends TextButton{

		private ProgressBar slider;

		public WeaponButton(Skin skin) {
			super("fjslk", skin);
			slider = new Slider(0, 1, 0.01f, false, skin);
			
			addActorBefore(getLabel(), slider);
			slider.setValue(0.5f);
		}
		
	}
}
