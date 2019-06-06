package com.badlogic.gdx.scenes.scene2d.ui;


import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import ninja.trek.BackgroundRenderer;
import ninja.trek.Item;
import ninja.trek.entity.Entity;
import ninja.trek.FontManager;
import ninja.trek.gen.GameInfo;
import ninja.trek.IntPixelMap;
import ninja.trek.Items;
import ninja.trek.MainSpaceCabal;
import ninja.trek.gen.Planet;
import ninja.trek.PlanetNode;
import ninja.trek.Quest;
import ninja.trek.QuestOption;
import ninja.trek.Ship;
import ninja.trek.Ship.EntityArray;
import ninja.trek.entity.ShipEntity;
import ninja.trek.SolarSystemGraph;
import ninja.trek.SystemPath;
import ninja.trek.entity.Weapon;
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
	public static final int SELL_COST_FACTOR = 3;
	private final World world;
	private final Table entityOtherActionTable;
	private final ProgressBar enemyHealthLabel;
	private DragAndDrop dnd;
	public UIActionButton[] entityActionButtons = new UIActionButton[Entity.jobNames.length];
	public UIActionButton[] entityOtherActionButtons = new UIActionButton[Entity.ButtonType.values().length];
	public UISystemButton[] shipSystemottomButtons = new UISystemButton[Ship.systemNames.length];
	private Table entityActionTable;
	private TextButton dragLabel;
	Entity currentEntity;
	protected Vector2 v = new Vector2();
	private Table leftTable;
	private Table rightTable;
	private Table shipSystemTable;
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
	public ProgressBar infoLabel;
	private TextField fileNameInput;
	private Window overConfirmSaveWindow;
	private Label errorLabel;
	private Window saveFileWindow;
	private TextButton saveWButton;
	public Table table;
	private Actor topSpacerActor;
	public WeaponButton[] weaponButtons;
	private Table weaponTable;
	private Window invWindow;
	private ItemDisplay invItemDisplay;
	private Skin skin;
	float fontScale = 2.5f;
	private Window middleTable;
	private Window shipControlWindow;
	private TextButton shipControlButton;
	private ChangeListener shipControlListener;
	private ChangeListener editButtonListener;
	private Table newGameSelectTable;
	private Window solarSystemWindow;
	private OrbitButton geoOrbitButton;
	private OrbitButton landOrbitButton;
	private OrbitButton ellOrbitButton;
	GameInfo info;
	public CheckBox randomFillButton;
	private Window entitiesWindow;
	private Table entitiesTable;
	private TextButton[] entityAddButtonRace;
	private EntityInfoButton[] entityInfoButtons;
	private QuestOptionDisplayPool questOptionPool;
	private TextButton saveWInternalButton;
	protected boolean saveInternal;
	private TextButton solarSystemJumpButton;
	private Actor mapButtonSpacer;
	public final String SPACER;
	private Window shopWindow;
	private ItemDisplay shopItemDisplay;
	private Label solarSystemFuelLabel;
	private int goeCost, orbitCost, landCost;

	public UI(final Stage stage, final World world, final FontManager fontManager, final TextureAtlas iconAtlas, final BackgroundRenderer background) {

		this.world = world;
		if (Gdx.app.getType() == ApplicationType.Android) {
			SPACER = "\n\n";
		}
		else { 
			SPACER = "\n";
			fontScale = 1.5f;
		}
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
		entityOtherActionTable = new Table();
		leftTable = new Table();
		rightTable = new Table();
		shipSystemTable = new Table();
		questOptionPool = new QuestOptionDisplayPool(skin, this);
		
		DragListener topDragL = new DragListenerSwap(this, entityActionTable, entityActionButtons){
			@Override
			public void swap(int a, int b) {
				ui.currentEntity.swap(a, b);
				ui.setEntity(ui.currentEntity);
			}};
		topDragL.setTapSquareSize(0f);
		entityActionTable.addListener(topDragL);
		entityActionTable.setTouchable(Touchable.enabled);
		for (int i = 0; i < Entity.jobNames.length; i++){
			UIActionButton btn = new UIActionButton(i, skin, entityActionTable);
			entityActionButtons[i] = btn;
			entityActionTable.add(btn).left();
			btn.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					UIActionButton uiBtn = (UIActionButton) event.getListenerActor();
					entity.disabledButton[uiBtn.index] = uiBtn.isChecked();
					//Gdx.app.log(TAG, "entity disable " + uiBtn.index + entity.disabledButton[uiBtn.index]);
					super.clicked(event, x, y);
				}
			});
			btn.setTouchable(Touchable.enabled);
			//Gdx.app.log(TAG, "butt " + i);
		}

		for (int i = 0; i < entityOtherActionButtons.length; i++){
			final UIOtherActionButton btn = new UIOtherActionButton(i, skin, entityActionTable, Entity.ButtonType.values()[i]);
			entityOtherActionButtons[i] = btn;
			//Gdx.app.log(TAG, "CREATE UI OTHER ACTION " + i + " " + btn.type + " " );
			if (btn.type.ordinal() != i) throw new GdxRuntimeException("" + btn.type + btn.type.ordinal());
			//entityOtherActionTable.add(btn).left();
			btn.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					//UIActionButton uiBtn = (UIActionButton) event.getListenerActor();
					//entity.disabledButton[uiBtn.index] = uiBtn.isChecked();
					//Gdx.app.log(TAG, "entity disable " + uiBtn.index + entity.disabledButton[uiBtn.index]);
					entity.handleOtherButton(btn.type);
					super.clicked(event, x, y);
				}
			});
			btn.setTouchable(Touchable.enabled);
			//Gdx.app.log(TAG, "butt " + i);
		}
		//actionTable.setBackground(skin.getDrawable("btn_default_pressed"));
		
		DragListener bottomDragL = new DragListenerSwap(this, shipSystemTable, shipSystemottomButtons){
			@Override
			public void swap(int a, int b) {
				Ship ship = world.getPlayerShip();
				ship.swap(a, b);
				ui.set(ship);
			}};
		bottomDragL.setTapSquareSize(0f);
		shipSystemTable.addListener(bottomDragL);
		shipSystemTable.setTouchable(Touchable.enabled);
		
		for (int i = 0; i < Ship.systemNames.length; i++){
			UISystemButton btn = new UISystemButton(i, skin, shipSystemTable, this, world);
			shipSystemottomButtons[i] = btn;
			shipSystemTable.add(btn).left();
			btn.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					UISystemButton uiBtn = (UISystemButton) event.getListenerActor();
					ship.disabledButton[uiBtn.index] = uiBtn.isChecked();
					lastPressedShipSystemButton = uiBtn;
					super.clicked(event, x, y);
				}
			});
			//bottomGroup.add(btn);
			btn.changeFont();
			btn.setTouchable(Touchable.enabled);
		}
		lastPressedShipSystemButton = shipSystemottomButtons[0];
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
		
		randomFillButton = new CheckBox("randFill", skin);
		
		ButtonGroup editTableGroup = new ButtonGroup();
		editTableGroup.setMinCheckCount(0);
		editTableGroup.add(fillBtn);
		editTableGroup.add(randomFillButton);
		//editTableGroup.add(xMirrorBtn);
		
		//editTable.row();
		brushSizeSlider = new Slider(1f, 10f, 1f, false, skin){
			
		};
		final Label brushSizeLabel = new Label("1", skin);

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
				if (ship == null) return;
				ShipEntity se = ship.getShipEntity();
				if (se == null){
					return;
				}
				setValue(se.health / (float)se.totalHealth);
				
				super.draw(batch, 1f);
			}
		};

		enemyHealthLabel =new ProgressBar(0f, 1f, 0.001f, false, skin){
			@Override
			public void draw(Batch batch, float parentAlpha) {
				//batch.setColor(Color.WHITE);
				if (ship == null) return;
				ShipEntity se = //ship.getShipEntity();
				world.getEnemyShip().getShipEntity();

				if (se == null){
					return;
				}
				setValue(se.health / (float)se.totalHealth);

				super.draw(batch, 1f);
			}
		};

		//leftTable.row();
		
		 Label infoTextLabel = new Label("", skin){
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
				int fires = ship.map.onFire.size;
				
				
				//setText("fires:" + fires);
				int reser = ship.getReservedCount();
				int boost = ship.map.damaged[Ship.WEAPON].size;
				//setText("needboost:" + boost + "\nres:" + reser + "\nfire:" + fires);
				setText("fps" + Gdx.graphics.getFramesPerSecond()
				+ "\nfire:" + ship.map.onFire.size
						+ "\nair:" + air
						);
				super.draw(batch, parentAlpha);
			}
		};
		  
		 
		
		
		
		//leftTable.add(editButton).left();
		
		destroyButton = new CheckBox("destroy", skin);
		destroyButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				destroyButton.setChecked(false);
				Ship ship = world.getPlayerShip();
                ship.resetPlace();
				ship.placeWeapon = true;
			}
		});
		
		editTableGroup.add(destroyButton);
		
		fireBtn = new CheckBox("fire", skin);
		
		editTableGroup.add(fireBtn);
		
		
		final TextButton weaponButton = new TextButton("add gun", skin);
		weaponButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				weaponButton.setChecked(false);
				Ship ship = world.getPlayerShip();
                ship.resetPlace();
				ship.placeWeapon = true;
				super.clicked(event, x, y);
			}
		});
		
		final TextButton weaponDeleteButton = new TextButton("del gun", skin);
		weaponDeleteButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				weaponDeleteButton.setChecked(false);
				Ship ship = world.getPlayerShip();
                ship.resetPlace();
				ship.deleteWeapon = true;
				super.clicked(event, x, y);
			}
		});
		
		
		final TextButton entitySpawnButton = new TextButton("spawn", skin);
		entitySpawnButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entitySpawnButton.setChecked(false);
				Ship ship = world.getPlayerShip();
                ship.resetPlace();
				ship.placeSpawn = true;

				super.clicked(event, x, y);
			}
		});
		
		
		
		final Window hullWindow = new Window("Generate Outer Hull Image", skin);
		final Slider hullRepsSlider = new Slider(1, 6, 1, false, skin);
		final Slider hullRadiusSlider = new Slider(1, 6, 1, false, skin);
		
		final TextButton openHullButton = new TextButton("hull", skin);
		openHullButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				openHullButton.setChecked(false);
				stage.addActor(hullWindow);
				super.clicked(event, x, y);
			}
		});
		
		final ButtonGroup hullGroup = new ButtonGroup();
		FileHandle[] tileFiles = Gdx.files.absolute(Gdx.files.internal("sources/").file().getAbsolutePath()).list();
		for (String path : tileFileLocations){
			TextButton b = new TextButton("" + path, skin);
			hullGroup.add(b);
			hullWindow.add(b).colspan(2);
			hullWindow.row();
		}
		
		
		final TextButton hullCloseButton = new TextButton("Close", skin);
		hullCloseButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hullCloseButton.setChecked(false);
				hullWindow.remove();
				super.clicked(event, x, y);
			}
		});
		hullWindow.row();
		final Label hullRepsLabel = new Label("reps(1)", skin);
		hullWindow.add(hullRepsLabel);
		hullWindow.add(hullRepsSlider);
		hullRepsSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullRepsLabel.setText("reps(" + (int)hullRepsSlider.getValue() + ")");
			}
		});
		hullWindow.row();
		final Label hullRadiusLabel = new Label("radius(1)", skin);
		hullWindow.add(hullRadiusLabel);
		hullWindow.add(hullRadiusSlider);
		hullRadiusSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullRadiusLabel.setText("radius(" + (int)hullRadiusSlider.getValue() + ")");
			}
		});
		
		
		
		
		final Slider hullXRadiusSlider = new Slider(1, 32, 1, false, skin);
		final Label hullXRadiusSliderLabel = new Label("expand X(1)", skin);
		final Slider hullPlusRadiusSlider = new Slider(1, 32, 1, false, skin);
		final Label hullPlusRadiusSliderLabel = new Label("expand +(1)", skin);
		hullXRadiusSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullXRadiusSliderLabel.setText("expand X(" + (int)hullXRadiusSlider.getValue() + ")");
			}
		});
		
		hullPlusRadiusSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullPlusRadiusSliderLabel.setText("expand +(" + (int)hullPlusRadiusSlider.getValue() + ")");
			}
		});
		
		final Slider hullFadeLengthSlider = new Slider(0, 32, 1, false, skin);
		final Label hullFadeLengthSliderLabel = new Label("fade (1)", skin);
		hullFadeLengthSlider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hullFadeLengthSliderLabel.setText("fade (" + (int)hullFadeLengthSlider.getValue() + ")");
			}
		});
		
		hullWindow.row();
		hullWindow.add(hullXRadiusSliderLabel);
		hullWindow.add(hullXRadiusSlider);
		hullXRadiusSlider.setValue(10);
		hullWindow.row();
		hullWindow.add(hullPlusRadiusSliderLabel);
		hullWindow.add(hullPlusRadiusSlider);
		hullPlusRadiusSlider.setValue(12);
		hullWindow.row();
		hullWindow.add(hullFadeLengthSliderLabel);
		hullWindow.add(hullFadeLengthSlider);

		
		final TextButton hullCalcButton = new TextButton("Generate", skin);
		hullCalcButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hullCalcButton.setChecked(false);
				Ship ship = world.getPlayerShip();
				CharSequence name = ((TextButton)hullGroup.getChecked()).getText();
				int reps = (int) hullRepsSlider.getValue();
				int radius = (int) hullRadiusSlider.getValue();
				int expandX = (int) hullXRadiusSlider.getValue();
				int expandPlus = (int) hullPlusRadiusSlider.getValue();
				int fadeSize = (int) hullFadeLengthSlider.getValue();
				
				ship.hull.calculate(ship, name.toString(), reps, radius, expandX, expandPlus, fadeSize);
				super.clicked(event, x, y);
			}
		});
		hullWindow.row();
		final TextButton deleteContigButton = new TextButton("delete non-contiguous", skin);
		deleteContigButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ship.fill.floodFillNonVaccuum(ship.map, ship.map.spawn.x, ship.map.spawn.y, Ship.WALL);
				for (int ax = 0; ax < ship.mapWidth; ax++)
					for (int ay = 0; ay < ship.mapHeight; ay++){
						if (ship.fill.get(ax, ay) != Ship.WALL)
							ship.map.set(ax, ay, Ship.VACCUUM);
					}
				ship.fill.clear();
				deleteContigButton.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		hullWindow.add(deleteContigButton);
		hullWindow.row();
		hullWindow.add(hullCalcButton);
		hullWindow.add(hullCloseButton);

		hullWindow.pack();
		
		//editTable.row();/////////////////////////////////////////////////////////////////////////
		
		
		saveWButton = new TextButton("save", skin);
		saveWInternalButton = new TextButton("save internal", skin);

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
		    				//saveGameFromClick(world, stage);

		            	}
		                //System.out.println("event="+event+" key="+keycode);
		                return super.keyUp(event, keycode);
		            };
		        };
		    }
		};
		final TextButton overYes = new TextButton("Yes", skin);
		overConfirmSaveWindow = new Window("File Exists. Overwrite?", skin);
		overConfirmSaveWindow.addListener(new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				overYes.setChecked(false);
				overConfirmSaveWindow.remove();
				saveFileWindow.remove();
				String name = fileNameInput.getText();
				Ship ship = world.getPlayerShip();
				
				saveShip(name, ship, saveInternal);
				return true;
			}
		});
			
		
		overConfirmSaveWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		final TextButton overNo = new TextButton("No", skin);
		
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

				saveShip(name, ship, saveInternal);
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
		final TextButton closeButton = new TextButton("cancel", skin);
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
				saveInternal = false;
				saveGameFromClick(world, stage, false);
				super.clicked(event, x, y);
			}
		});
		saveFileWindow.add(saveWButton);
		saveWInternalButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveInternal = true;
				saveGameFromClick(world, stage, true);
				super.clicked(event, x, y);
			}
		});
		
		saveFileWindow.add(saveWInternalButton);
		saveFileWindow.pack();
		saveFileWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		
		final TextButton saveButton = new TextButton("Save", skin);
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
		
		final Window loadWindow = new Window("Select Ship to Load", skin);
		loadWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);

		loadGroup = new ButtonGroup();
		
		loadButtonInWindow = new TextButton("Load Selected", skin);
		loadButtonInWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);

		loadButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Ship ship = world.getPlayerShip();
				LoadLabel load = (LoadLabel) loadGroup.getChecked();
				load.loadFileInto(ship, world);
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
		
		final TextButton loadButton = new TextButton("Load", skin);
		loadButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				populateLoadWindow(loadWindow);
				stage.addActor(loadWindow);
				loadButton.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		
		final TextButton startButton = new TextButton("Test Battle", skin);
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
		
		final Window newShipWindow = new Window("Select Settings for New Ship", skin);
		newShipWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);


		/*final TextButton createShipButtonInWindow = new TextButton("Create", skin);
		createShipButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				createShipButtonInWindow.setChecked(false);
				newShipWindow.remove();

				
				world.newPlayerShip();
				super.clicked(event, x, y);
			}
		});*/
        final TextButton createShipButtonInWindow2 = new TextButton("Create drone", skin);
        createShipButtonInWindow2.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createShipButtonInWindow2.setChecked(false);
                newShipWindow.remove();

				EntityArray entities = Pools.obtain(EntityArray.class);
				Ship sh = world.getPlayerShip();
				sh.load(new IntPixelMap(64, 64), entities, sh.hull.getTexture(), sh.inventory, iconAtlas);
				sh.categorizeSystems();
               // world.newPlayerShip();
                super.clicked(event, x, y);
            }
        });
        final TextButton createShipButtonInWindow3 = new TextButton("Create 256", skin);
        createShipButtonInWindow3.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createShipButtonInWindow3.setChecked(false);
                newShipWindow.remove();

				EntityArray entities = Pools.obtain(EntityArray.class);
				Ship sh = world.getPlayerShip();
				sh.load(new IntPixelMap(256, 256), entities, sh.hull.getTexture(), sh.inventory, iconAtlas);
				sh.categorizeSystems();
                //world.newPlayerShip();
                super.clicked(event, x, y);
            }
        });
        final TextButton createShipButtonInWindow4 = new TextButton("Create 512", skin);
        createShipButtonInWindow4.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createShipButtonInWindow4.setChecked(false);
                newShipWindow.remove();

                EntityArray entities = Pools.obtain(EntityArray.class);
				Ship sh = world.getPlayerShip();
                sh.load(new IntPixelMap(512, 512), entities, sh.hull.getTexture(), sh.inventory, iconAtlas);
                sh.categorizeSystems();
               // world.newPlayerShip();
                super.clicked(event, x, y);
            }
        });
		final TextButton cancelCreateShipButtonInWindow = new TextButton("cancel", skin);
		cancelCreateShipButtonInWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				cancelCreateShipButtonInWindow.setChecked(false);
				newShipWindow.remove();
				super.clicked(event, x, y);
			}
		});
		newShipWindow.row();
		newShipWindow.row();
		//newShipWindow.add(createShipButtonInWindow1);
		newShipWindow.add(createShipButtonInWindow2).row();
		newShipWindow.add(createShipButtonInWindow3).row();
		newShipWindow.add(createShipButtonInWindow4).row();
		newShipWindow.add(cancelCreateShipButtonInWindow).row();
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
		
		
		final CheckBox hullShowButton = new CheckBox("show hull", skin);
		hullShowButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ship.showHull = !ship.showHull;
				hullShowButton.setChecked(ship.showHull);
				event.handle();
				super.clicked(event, x, y);
			}
		});
		final CheckBox hullBackToggleButton = new CheckBox("hull front", skin);
		hullBackToggleButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ship.hullFront = !ship.hullFront;
				hullBackToggleButton.setChecked(ship.hullFront);
				event.handle();
				super.clicked(event, x, y);
			}
		});
		
		final UI ui = this;
		final TextButton solarSystemButton = new TextButton("SS map", skin);
		solarSystemButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				solarSystemButton.setChecked(false);
				world.showSolarSystemView(ui);
				super.clicked(event, x, y);
			}
		});
		
		solarSystemJumpButton = new TextButton("MAP", skin);
		mapButtonSpacer = new Label("  ", skin);
	
		//mapButtonSpacer = new WidgetGroup();
		
		solarSystemJumpButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				solarSystemJumpButton.setChecked(false);
				background.unRotate();
				world.showSolarSystemView(ui);
				super.clicked(event, x, y);
			}
		});
		
		
		makeEntitiesWindow(stage, fontManager);
		final TextButton entitiesButton = new TextButton("Entities", skin);
		entitiesButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entitiesButton.setChecked(false);
				populateEntitiesWindow(fontManager);
				stage.addActor(entitiesWindow);
				super.clicked(event, x, y);
			}

			
		});
		
		
		editTable.add(saveButton).left();
		editTable.row();
		editTable.add(loadButton).left();
		editTable.row();
		editTable.add(startButton).left();
		editTable.row();
		editTable.add(newShipButton).left();
		editTable.row();
		editTable.add(solarSystemButton);
		editTable.row();
		
		editTable.add(brushSizeSlider);
		//editTable.row();
		editTable.add(brushSizeLabel);
		editTable.row();
		editTable.add(xMirrorBtn).left();
		editTable.row();
		editTable.add(fillBtn).left();
		editTable.row();
		editTable.add(randomFillButton).left();
		editTable.row();
		editTable.add(editLineButton).left();
		editTable.row();
		Actor  doorBtn =  new TextButton(" Add Door", skin);
		doorBtn.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//ll
				Ship ship = world.getPlayerShip();
				ship.resetPlace();
				ship.placeDoor = true;
			}
		});

		Actor  doorDeleteBtn =  new TextButton(" Delete Door", skin);
		doorDeleteBtn.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//ll
				Ship ship = world.getPlayerShip();
				ship.resetPlace();
				ship.deleteDoor = true;
			}
		});
		editTable.add(doorBtn);
		editTable.row();
		editTable.add(doorDeleteBtn);
		editTable.row();

		editTable.add(weaponButton).left();
		editTable.row();
		editTable.add(weaponDeleteButton).left();
		editTable.row();
		editTable.add(entitySpawnButton).left();
		editTable.row();
		editTable.add(entitiesButton);
		
		editTable.row();
		editTable.add(destroyButton).left();
		editTable.row();
		editTable.add(fireBtn).left();
		editTable.row();
		editTable.add(hullShowButton).left();
		editTable.row();
		editTable.add(hullBackToggleButton).left();
		editTable.row();
		editTable.add(openHullButton).left();
		editTable.row();
		
		final ScrollPane editPane = new ScrollPane(emptyEditTable);
		editPane.setOverscroll(false, true);
		Array<EventListener> listeners = editPane.getListeners();
		editPane.removeListener(listeners.get(listeners.size - 1));

		editPane.addListener(new InputListener() {
			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				editPane.resetFade();
				v.set(x, y);
				//localToStageCoordinates(v);
				//Gdx.app.log(TAG, "fdskjl " + getWidth() + "  " + v);
				if (v.x < 0 || v.y < 0 || v.x > editPane.getWidth() || v.y > editPane.getHeight()) return false;

				if (editPane.scrollY)
					editPane.setScrollY(editPane.amountY + editPane.getMouseWheelY() * amount);
				else if (editPane.scrollX) //
					editPane.setScrollX(editPane.amountX + editPane.getMouseWheelX() * amount);
				else
					return false;
				return true;
			}
		});

		
		//leftTable.add(infoLabel).left();
		//leftTable.add(new Label("", skin)).expandX();
		shipControlWindow = new Window("", skin);
		shipControlButton = new TextButton("SHIP", skin);

		editButton = new CheckBox("edit", skin);
		editButtonListener = new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.getPlayerShip().editMode = editButton.isChecked();
				//event.handle();
				if (editButton.isChecked()){
					emptyEditTable.add(editTable);
					//shipControlWindow.getCell(editPane).height(Gdx.graphics.getHeight()/2);
					shipControlWindow.pack();
					v.set(0, 0);
					shipControlButton.localToStageCoordinates(v);
					shipControlWindow.setPosition(v.x, v.y, Align.topLeft);
				} else {
					editTable.remove();
					v.set(0, 0);
					shipControlButton.localToStageCoordinates(v);
					shipControlWindow.pack();
					shipControlWindow.setPosition(v.x, v.y, Align.topLeft);
				}
				set(ship);
				table.invalidate();
				stage.setScrollFocus(editPane);
			}
			
		};
		editButton.addListener(editButtonListener);
		
		leftTable.row();
		
		
		
		leftGroup.add(editButton);
		

		weaponButtons = new WeaponButton[MAX_WEAPON_BUTTONS];
		weaponTable = new Table();
		weaponTable.add(solarSystemJumpButton);

		for (int i = 0; i < MAX_WEAPON_BUTTONS; i++){
			final int index = i;
			weaponButtons[i] = new WeaponButton(skin, i);
			weaponButtons[i].addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					weaponButtons[index].setChecked(false);
					world.targettingIndex = index;
					world.getPlayerShip().cancelWeaponTarget(index);
					Ship enemy = world.getEnemyShip();
					enemy.zoomInForTarget();
					super.clicked(event, x, y);
				}
			});
		}
		Table topTable = new Table();
		Table shieldControl = new Table();
		final TextButton shieldMinus = new TextButton("-", skin);
		shieldMinus.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ShipEntity shipE = ship.getShipEntity();;
				if (shipE != null){
					shipE.shieldTotal--;
					shipE.shieldTotal = Math.max(0,  shipE.shieldTotal);
				}
				shieldMinus.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		Label shieldLabel = new Label("Shields(0/0)", skin){
			int prevShield, prevTotal;
			@Override
			public void draw(Batch batch, float parentAlpha) {
				if (ship != null) {
					ShipEntity shipE = ship.getShipEntity();;
					if (shipE != null){
						if (shipE.shield != prevShield || shipE.shieldTotal != prevTotal){
							prevShield = shipE.shield;
							prevTotal = shipE.shieldTotal;
							setText("Shields("+shipE.shield + "/" + shipE.shieldTotal + ")");
						}
					}
				}
				super.draw(batch, parentAlpha);
			}
		};
		final TextButton shieldPlus = new TextButton("+", skin);
		shieldPlus.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ShipEntity shipE = ship.getShipEntity();;
				if (shipE != null){
					shipE.shieldTotal++;
				}
				shieldPlus.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		shieldControl.add(shieldMinus);
		shieldControl.add(shieldLabel);
		shieldControl.add(shieldPlus);


		
		shipControlWindow.add(shieldControl);
		shipControlWindow.row();
		shipControlWindow.add(editButton).left();
		shipControlWindow.row();
		shipControlWindow.add(editPane);

		shipControlWindow.getTitleLabel().setTouchable(Touchable.disabled);
		shipControlWindow.getTitleTable().setTouchable(Touchable.disabled);
		shipControlWindow.setTouchable(Touchable.childrenOnly);
		shipControlListener = new ChangeListener(){
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {

				if (shipControlButton.isChecked()){
					stage.addActor(shipControlWindow);
					v.set(0, 0);
					shipControlButton.localToStageCoordinates(v);
					//shipControlWindow.setHeight(Gdx.graphics.getHeight()/2);
					shipControlWindow.getCell(editPane).maxHeight(Gdx.graphics.getHeight()/3 * 2);
					shipControlWindow.pack();











					shipControlWindow.setPosition(v.x, v.y, Align.topLeft);
					stage.setScrollFocus(editPane);
					//Gdx.app.log(TAG, "control CHECKED");
				} else {
					//Gdx.app.log(TAG, "control NOT CHECKED");
					
					shipControlWindow.remove();
				}



				
				
			
				
			}
		};
		shipControlButton.addListener(shipControlListener );
		
		
		
		topTable.add(infoLabel).top().left();
		topTable.add(shipSystemTable);
		topTable.row();
		topTable.add(shipControlButton).left();
		topTable.add(weaponTable).right().colspan(13);//.right();
		topTable.row();
		topTable.add(infoTextLabel);//.colspan(3).left();


		newGameSelectTable = new Table();
		final TextButton nextShipButton = new TextButton(SPACER + "Next Ship >>" + SPACER, skin);
		nextShipButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.showNextNewGameShip();
				set(world.getPlayerShip());
				setEntity(null);
				nextShipButton.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		final TextButton prevShipButton = new TextButton(SPACER + "<< Prev Ship" + SPACER, skin);
		prevShipButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.showPrevNewGameShip();
				set(world.getPlayerShip());
				setEntity(null);
				prevShipButton.setChecked(false);
				super.clicked(event, x, y);
			}
		});
		
		middleTable = new Window("Space Cabal", skin);
		middleTable.setTouchable(Touchable.childrenOnly);
		middleTable.getTitleTable().setTouchable(Touchable.disabled);
		
		final TextButton newGameButton = new TextButton("New Game", skin);
		final TextButton startNewGameButton = new TextButton(SPACER + "Start Campaign" + SPACER, skin);
		startNewGameButton.addListener(new ClickListener(){

			@Override
			public void clicked(InputEvent event, float x, float y) {
				startNewGameButton.setChecked(false);
				info = world.startNewGame();
				set(world.getPlayerShip());
				newGameSelectTable.remove();
				super.clicked(event, x, y);
			}
		});
		newGameButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.startNewGameMenu();
				set(world.getPlayerShip());
				middleTable.remove();        
				newGameButton.setChecked(false);
				newGameSelectTable.add(prevShipButton).bottom();
				newGameSelectTable.add(new Actor()).expand();
				newGameSelectTable.add(startNewGameButton).bottom();
				newGameSelectTable.add(new Actor()).expand();
				newGameSelectTable.add(nextShipButton).bottom();
				stage.addActor(newGameSelectTable);
				newGameSelectTable.setFillParent(true);
				super.clicked(event, x, y);
			}
		});
		Table middleMenuTable = new Table();
		middleTable.add(middleMenuTable).center();
		middleMenuTable.add(newGameButton);
		middleMenuTable.row();
		final TextButton customShipButton = new TextButton("Ship Editor", skin);
		customShipButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				customShipButton.setChecked(false);
				middleTable.remove();
				shipControlButton.setChecked(true);
				set(world.getPlayerShip());
				//editButtonListener.changed(null, null);
				//shipControlListener.clicked(null, 0, 0);
				//shipControlButton.setChecked(false);
				//editButton.setChecked(false);
				editButton.setChecked(true, true);
				shipControlButton.setChecked(true, true);
				
				
				super.clicked(event, x, y);
			}
		});
		middleMenuTable.add(customShipButton);
		middleMenuTable.row();
		middleTable.pack();
		middleTable.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		//table.row();
		table.setFillParent(true);
		table.add(topTable).top().left();
		table.row();
		//table.add(new Actor()).expand();
		//table.row();
		
		table.add(leftTable).left().top();
		
		
		table.add(new Actor()).expand();
		table.add(rightTable);
		table.row();
		table.add(enemyHealthLabel).right().row();
		//table.add(bottomWeaponTable).left();
		table.add(entityOtherActionTable);
		table.row();
		table.add(entityActionTable).left();
		
		table.setTouchable(Touchable.enabled);
		stage.addActor(table);
		stage.addActor(middleTable);
		//stage.addActor(dragLabel);
		table.layout();
		
		topSpacerActor = new Actor();
		
		makeInventoryWindow(skin);
		makeShopWindow(skin);

		makeSolarSystemWindow(skin, world);
		
		makeQuestWindow(skin);


	}
	private void makeEntitiesWindow(final Stage stage, final FontManager fontManager) {
		entitiesWindow = new Window("Entities", skin);
		final Window entityAddWindow = new Window("Add Entity", skin);
		entitiesTable = new Table();
		final TextButton entityAddButton = new TextButton("Add", skin);
		entityAddButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entityAddButton.setChecked(false);
				if (ship == null) return;
				entityAddWindow.pack();
				entityAddWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
				stage.addActor(entityAddWindow);
				
				super.clicked(event, x, y);
			}
		});
		
		entityAddButtonRace = new TextButton[Entity.raceNames.length];
		for (int i = 0; i < Entity.raceNames.length; i++) {
			final int raceIndex = i;
			entityAddButtonRace[i] = new TextButton(Entity.raceNames[i], skin);
			entityAddWindow.add(entityAddButtonRace[i]);
			entityAddButtonRace[i].addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					entityAddWindow.remove();
					entityAddButtonRace[raceIndex].setChecked(false);
					ship.addEntity(raceIndex);
					populateEntitiesWindow(fontManager);
				}
			});
		}
		final TextButton entityAddWindoCloseButton = new TextButton("close", skin);
		entityAddWindoCloseButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entityAddWindoCloseButton.setChecked(false);
				entityAddWindow.remove();
				super.clicked(event, x, y);
			}
		});
		entityAddWindow.add(entityAddWindoCloseButton);
		
		final TextButton entityWindowCloseButton = new TextButton("close", skin);
		entityWindowCloseButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				entityWindowCloseButton.setChecked(false);
				entitiesWindow.remove();
				super.clicked(event, x, y);
			}
		});

		ScrollPane entitiesPane = new ScrollPane(entitiesTable);
		//entitiesPane.setHeight(Gdx.graphics.getHeight() * .7f);

		entitiesWindow.add(entityAddButton);
		entitiesWindow.add(entityWindowCloseButton);
		entitiesWindow.row();
		entitiesWindow.add(entitiesPane);
		entityInfoButtons = new EntityInfoButton[Ship.MAX_ENTITIES];
		for (int i = 0; i < Ship.MAX_ENTITIES; i++) {
			entityInfoButtons[i] = new EntityInfoButton(i, skin);
			
		}
		
	}
	private void populateEntitiesWindow(FontManager fontManager) {
		EntityArray list = ship.getEntities();
		for (int i = 0; i < list.size; i++) {
			Entity e = list.get(i);
			if (e instanceof Weapon) continue;
			entityInfoButtons[i].set(e, fontManager);;
			entitiesTable.add(entityInfoButtons[i]);
			entitiesTable.row();
		}
		entitiesWindow.pack();
		float maxH = Gdx.graphics.getHeight() * .7f;
		if (entitiesWindow.getHeight() > maxH)
			entitiesWindow.setHeight(maxH);


		entitiesWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);

	}



	public void buy(ItemDisplay.ItemButton it, Ship player, Ship shop) {
		Item def = Items.getDef(it.itemID);
		if (player.getShipEntity().credits < def.cost) return;
		player.inventory.add(it.itemID);
		shop.unequipWeaponByID(it.itemID);
		shop.inventory.removeIndex(it.index);
		player.getShipEntity().credits -= def.cost;
		toggleShop(player, shop);
		toggleShop(player, shop);
		set(ship);
	}

	public void sell(ItemDisplay.ItemButton it, Ship player, Ship shop) {
		Item def = Items.getDef(it.itemID);
		if (shop.getShipEntity().credits < def.cost/SELL_COST_FACTOR) return;
		shop.inventory.add(it.itemID);
        player.unequipWeaponByID(it.itemID);
		player.inventory.removeIndex(it.index);
		shop.getShipEntity().credits += def.cost/SELL_COST_FACTOR;
		player.getShipEntity().credits -= def.cost/SELL_COST_FACTOR;
		toggleShop(player, shop);
		toggleShop(player, shop);
		set(ship);
	}

	private static class EntityInfoButton extends TextButton{

		private int index;

		public EntityInfoButton(int i, Skin skin) {
			super("entity", skin);
			index = i;
		}

		public void set(Entity e, FontManager fontManager) {
			setText(e.glyph + " : " + Entity.raceNames[e.font]);
			getLabel().getStyle().font = fontManager.getFont(e.font);
			//col.set(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
			//getLabel().setColor(col);;
		}
		
	}
	static Color col = new Color();
	private void makeSolarSystemWindow(Skin skin2, final World world) {

		solarSystemWindow = new Window("Planet Name", skin);
		solarSystemWindow.setTouchable(Touchable.enabled);
		solarSystemWindow.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				event.handle();
			}
		});
		Table solarButtonsTable = new Table();
		geoOrbitButton = new OrbitButton("Low Orbit", skin);
		geoOrbitButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				geoOrbitButton.setChecked(false);
				if (world.goToOrbit(GameInfo.ORBIT_ORBIT, geoOrbitButton.cost))
					solarSystemWindow.remove();
				super.clicked(event, x, y);
			}
		});
		landOrbitButton = new OrbitButton("Land", skin);
		landOrbitButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				landOrbitButton.setChecked(false);
				if (world.goToOrbit(GameInfo.ORBIT_LANDED, landOrbitButton.cost))
					solarSystemWindow.remove();
				super.clicked(event, x, y);
			}
		});
		
		ellOrbitButton = new OrbitButton("Elliptical Orbit", skin);
		ellOrbitButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ellOrbitButton.setChecked(false);
				if (world.goToOrbit(GameInfo.ORBIT_ELLIPTICAL, ellOrbitButton.cost))
					solarSystemWindow.remove();
				super.clicked(event, x, y);
			}
		});
		solarSystemFuelLabel = new Label("Fuel: 99999", skin);


		solarButtonsTable.add(solarSystemFuelLabel).row();
		solarButtonsTable.add(ellOrbitButton);
		solarButtonsTable.row();
		solarButtonsTable.add(landOrbitButton);
		solarButtonsTable.row();
		solarButtonsTable.add(geoOrbitButton);



		//solarSystemWindow.add(planetInfoLabel).width(Gdx.graphics.getWidth());;
		//solarSystemWindow.row();
		solarSystemWindow.add(solarButtonsTable );
		solarSystemWindow.pack();
		Gdx.app.log(TAG, "make SS window" + solarSystemFuelLabel);
		
		solarSystemWindow.setPosition(Gdx.graphics.getWidth(), 0, Align.bottomRight);
	}
	private Ship ship;
	private Entity entity;
	public UISystemButton lastPressedShipSystemButton;
	private Window questWindow;
	private Label questTextLabel;

	public void addSolarSystemWindow(Stage stage, int selectedPlanet) {
		//landOrbitButton.setDisabled(true);
		//geoOrbitButton.setDisabled(true);
		//ellOrbitButton.setDisabled(true);

		stage.addActor(solarSystemWindow);
		setPlanetInfo(selectedPlanet);
		//solarSystemWindow.pack();
		//stage.addActor(planetInfoLabel);
		//planetInfoLabel.setPosition(0, 0, Align.bottomLeft);
	}
	public void setPlanetInfo(int selectedPlanet) {
		if (selectedPlanet == -1) {
			solarSystemWindow.getTitleLabel().setText(info.systems[info.currentSystem].sun.toString());
		} else {
			solarSystemWindow.getTitleLabel().setText(info.systems[info.currentSystem].planets[selectedPlanet].toString());
		}
		setPathCost(geoOrbitButton, "Orbit ", selectedPlanet, PlanetNode.NodeType.ORBIT);
		setPathCost(ellOrbitButton, "Elliptical orbit ", selectedPlanet, PlanetNode.NodeType.ELLIPTICAL);
		setPathCost(landOrbitButton, "Land ", selectedPlanet, PlanetNode.NodeType.LAND);

        solarSystemWindow.pack();
		//landOrbitButton;
		//ellOrbitButton;
	}

	private int setPathCost(OrbitButton button, String text, int selectedPlanet, PlanetNode.NodeType type) {
		PlanetNode startNode;
		Planet planet;
		if (info.currentPlanet == -1){
			planet = info.systems[info.currentSystem].sun;
			startNode = world.solarSystemGraph.getSolar(info.currentOrbitalDepth);
		} else {
			planet = info.systems[info.currentSystem].planets[info.currentPlanet];
			if (planet.parent == -1){
				startNode = world.solarSystemGraph.getNode(info.currentPlanet, info.currentOrbitalDepth);
			} else {
				startNode = world.solarSystemGraph.getNode(planet.parent, planet.parentOrder, info.currentOrbitalDepth);
			}
		}
		Planet sel;
		PlanetNode endNode = null;
		if (selectedPlanet == -1){
			sel = info.systems[info.currentSystem].sun;
			endNode = world.solarSystemGraph.getSolar(type);

		} else {
			sel = info.systems[info.currentSystem].planets[selectedPlanet];
			if (sel.parent == -1){
				endNode = world.solarSystemGraph.getNode(selectedPlanet, type);
			} else{
				endNode = world.solarSystemGraph.getNode(sel.parent, sel.parentOrder, type);
			}
		}
		Heuristic<PlanetNode> heur = SolarSystemGraph.heuristic;
		SystemPath outPath = new SystemPath();
		world.universePath.searchConnectionPath(startNode, endNode, heur, outPath);
		Gdx.app.log(TAG, "found path " + startNode.type + " " + endNode.type);
		int orbitCost = (int) outPath.cost();
		button.setText(text + orbitCost);
		button.cost = orbitCost;
		if (outPath.getCount() == 0){
			button.setDisabled(true);
			button.setTouchable(Touchable.disabled);
		} else {
			button.setDisabled(false);
			button.setTouchable(Touchable.enabled);
		}
		return orbitCost;
	}

	private void makeInventoryWindow(Skin skin) {
		invWindow = new Window("Inventory", skin);
		invItemDisplay = new ItemDisplay(skin, invWindow, this);
		invWindow.add(invItemDisplay);
	}

	public void toggleInventory(Ship playerShip) {
		if (invWindow.getStage() == null){
			
			invItemDisplay.setLeft(playerShip);
			invItemDisplay.setRight(null);
			invWindow.pack();
			invWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
			table.addActor(invWindow);
		} else invWindow.remove();
		
		//Gdx.app.log(TAG, "open inv");
	}

	private void makeShopWindow(Skin skin) {
		shopWindow = new Window("Shop", skin);
		shopWindow.getTitleTable().getCell(shopWindow.getTitleLabel()).center().colspan(3).expand().fill();
		shopItemDisplay = new ItemDisplay(skin, shopWindow, this);
		shopWindow.add(shopItemDisplay);
        shopItemDisplay.setShop();
	}

	public void toggleShop(Ship playerShip, Ship shopShip) {
		if (shopWindow.getStage() == null){
			shopItemDisplay.setLeft(playerShip);
			shopItemDisplay.setRight(shopShip);
			shopWindow.pack();
			shopWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
			table.addActor(shopWindow);
		} else shopWindow.remove();

	}

	public void set(Ship ship) {
		this.ship = ship;
		shipSystemTable.clearChildren();
		weaponTable.clearChildren();
		//bottomTable.row();
		if (ship != null){
			
			for (int i = 0; i < ship.systemButtonOrder.length; i++){
				if (ship.editMode || (ship.systemButtonOrder[i] != Ship.VACCUUM && ship.systemButtonOrder[i] != Ship.FLOOR && ship.systemButtonOrder[i] != Ship.WALL )){
					shipSystemTable.add(shipSystemottomButtons[ship.systemButtonOrder[i]]);
					shipSystemottomButtons[i].getLabel().setFontScale(fontScale);
				}
			}
			//currentEntity = e;
			shipSystemTable.layout();
			
			if (!world.isNewGameScreen())weaponTable.add(solarSystemJumpButton).left();
			weaponTable.add(mapButtonSpacer).expandX().fillX();
			//weaponTable.add(new Label("  ", skin)).fillX().expandX();

			float maxW = 0, total = 0;
			for (int i = 0; i < shipSystemottomButtons.length; i++){
				if (ship.editMode || (i != Ship.VACCUUM && i != Ship.FLOOR && i != Ship.WALL))
				{
					float w = shipSystemottomButtons[i].getWidth();
					maxW += w;
					total++;
				}
			}
			maxW = (int) (( Gdx.graphics.getWidth() - infoLabel.getWidth() ) / total);
			
			for (int i = 0; i < shipSystemottomButtons.length; i++){
				if (ship.editMode || (ship.systemButtonOrder[i] != Ship.VACCUUM && ship.systemButtonOrder[i] != Ship.FLOOR && ship.systemButtonOrder[i] != Ship.WALL)){
					if (shipSystemTable.getCell(shipSystemottomButtons[ship.systemButtonOrder[i]]) == null) throw new GdxRuntimeException("null " + i);
					shipSystemTable.getCell(shipSystemottomButtons[ship.systemButtonOrder[i]]).width(maxW).pad(0).space(0);
					
					float sc = (maxW / shipSystemottomButtons[i].getLabel().getWidth()) * .8f;
					sc = Math.min(1f, sc);
					sc *= fontScale;
					shipSystemottomButtons[i].getLabel().setFontScaleX(sc);
					shipSystemottomButtons[i].setChecked(ship.disabledButton[i]);;
				}
				
			}
			int i = 0;
			for (Entity e : ship.getEntities()){
				//Gdx.app.log(TAG, "look at e " + e.getClass());
				if (e instanceof Weapon){
					Weapon w = (Weapon) e;
					//ship.equippedWeapons[w.index];
					WeaponButton b = weaponButtons[i];
					if (w.equippedItemID != -1){
						b.setText(Items.getDef(w.equippedItemID).name);
					} else b.setText(" ");
					//Gdx.app.log(TAG, "ADDDD WEAPONNNNNNNNNNNNN " + b.getText());
					weaponTable.add(b);
					
					
					i++;
				}
			}
			
			weaponTable.row();
			weaponTable.layout();

			if (ship.getShipEntity() != null){
				solarSystemFuelLabel.setText("Fuel:"+ship.getShipEntity().fuel);
				//Gdx.app.log(TAG, "update fuel label " + ship.getShipEntity().fuel);
			}


		}else {
			shipSystemTable.add(topSpacerActor).expandX();
		}
		
	}

	public void setEntity(Entity e){
		entity = e;
		Table actionTable = this.entityActionTable;
		Table otherActionTable = this.entityOtherActionTable;

		actionTable.clearChildren();
		otherActionTable.clearChildren();
		if (e != null){
			UIActionButton[] buttons2 = null;
			buttons2 = entityActionButtons;
			UIActionButton[] otherButtons = entityOtherActionButtons;
			if (e.otherButtons != null){
				for (int i = 0; i < e.otherButtons.length; i++){
					//buttons2[i].getLabel().setFontScale(fontScale);
					//Gdx.app.log(TAG, "other btn " + otherButtons.length + " " + e.otherButtons.length);
					otherActionTable.add(otherButtons[e.otherButtons[i].ordinal()]);
				}
			}
			if (e.buttonOrder != null){
				for (int i = 0; i < e.buttonOrder.length; i++){
					buttons2[i].getLabel().setFontScale(fontScale);
					actionTable.add(buttons2[e.buttonOrder[i]]);
				}
			}
			currentEntity = e;
			int maxW, total = 0;
			//float scale = skin.get("default-font", BitmapFont.class).getData().scaleX;
			//while (tooBig && scale > .3f)
			{
				maxW = 0;
				total = 0;
				SnapshotArray<Actor> actButtons = actionTable.getChildren();
				for (int i = 0; i < actButtons.size; i++){
					{
						float w = actButtons.get(i).getWidth();
						//if (buttons[i].getWidth() <= buttons[i].getMinWidth()+1)
						maxW += w;
						total++;
					}
				}
			}
			if (total == 0){
				maxW = 0;
			} else {
				maxW = (int) (( Gdx.graphics.getWidth()  ) / total);
			}
			actionTable.layout();
			table.layout();

			for (int i = 0; i < buttons2.length; i++){
				Cell<UIActionButton> cell = actionTable.getCell(buttons2[i]);
				if (cell != null){
					cell.width(maxW).pad(0).space(0);

				} else {
					//Gdx.app.log(TAG, "no cell " + i + "  ");
				}
				float sc = (maxW / buttons2[i].getLabel().getWidth()) * .8f;
				sc = Math.min(1f, sc);
				sc *= fontScale;
				buttons2[i].getLabel().setFontScaleX(sc);
				buttons2[i].setChecked(e.disabledButton[i]);;
			}
		}
		actionTable.invalidate();
	}
	
	public void reset(){

	}

	public void setPreviewLine(Vector3 v, Vector3 v2) {
		previewA .set(v);
		previewB.set(v2);
	}
	private boolean fileExists(String name, boolean internal) {
		FileHandle file = Gdx.files.external(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
		if (internal)
			file = Gdx.files.internal(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
			
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
		FileHandle folder = Gdx.files.external(MainSpaceCabal.SHIP_SAVE_LOCATION);
		FileHandle[] list = folder.list(MainSpaceCabal.MAP_FILE_EXTENSION);
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

	public void saveShip(String name, Ship ship, boolean internal) {
		//Gdx.app.log(TAG, "actually save " + name + Main.MAP_FILE_EXTENSION);
		Gdx.app.log(TAG, "SAVE" + internal);

		FileHandle file;
		if (internal) {
			file = Gdx.files.internal(MainSpaceCabal.SHIP_SAVE_LOCATION);
			file = Gdx.files.absolute(file.file().getAbsolutePath());
			//file.mkdirs();
			
			file = file.child( name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
			Gdx.app.log(TAG, "INTERNAL" + file.file().getAbsolutePath());
		} else {
			
			file = Gdx.files.external(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_FILE_EXTENSION);
			//if (true) throw new GdxRuntimeException("jfsdkl");
		}
		FileHandle entityFile = file.sibling(name + "." + MainSpaceCabal.ENTITY_FILE_EXTENSION);
		FileHandle blocksFile = file.sibling( name + "." + MainSpaceCabal.MAP_BLOCKS_FILE_EXTENSION);
		FileHandle invFile = file.sibling( name + "." + MainSpaceCabal.MAP_INVENTORY_FILE_EXTENSION);
		
		Json json = Pools.obtain(Json.class);
		Gdx.app.log(TAG, "writing ship data");
		String string = json.toJson(ship.map);
		file.writeString(string, false);
		Gdx.app.log(TAG, "writing ship blocks");
		try {
			ObjectOutputStream stream = new ObjectOutputStream (new GZIPOutputStream(blocksFile.write(false)));
			stream.writeObject(ship.map.getRawBlocks());
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Gdx.app.log(TAG, "writing entities ");

		String entities = json.prettyPrint(ship.getEntities());
		entityFile.writeString(entities, false);
		String invS = json.prettyPrint(ship.inventory);
		invFile.writeString(invS, false);
		Gdx.app.log(TAG, "writing hull ");
		FileHandle hullFile = file.sibling(name + "." + MainSpaceCabal.MAP_HULL_EXTENSION);
		
		Pixmap hullPix =ship.hull.getPixmap();;
		if (hullPix != null)
			PixmapIO.writePNG(hullFile, hullPix );
		//savePreview(name, ship);
		Pools.free(json);
	}
	
	
	
	private void savePreview(String name, Ship ship) {
		FileHandle file = Gdx.files.external(MainSpaceCabal.SHIP_SAVE_LOCATION + name + "." + MainSpaceCabal.MAP_PREVIEW_EXTENSION);

		ship.savePreview(file, ship);
	}
	private void saveGameFromClick(World world, Stage stage, boolean internal) {
		String name = fileNameInput.getText();
		if (fileExists(name, internal)){
			stage.addActor(overConfirmSaveWindow);
			;
			stage.setKeyboardFocus(overConfirmSaveWindow);
		} else if (isValidFileName(name, errorLabel)){
			
			Ship ship = world.getPlayerShip();
			saveShip(name, ship, internal);
			saveFileWindow.remove();
		} 
		saveWButton.setChecked(false);
		
	}
	
	public void resize(){
		table.invalidate();
		set(ship);
		setEntity(entity);
		middleTable.pack();
		middleTable.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		
		shipControlWindow.remove();
		shipControlButton.setChecked(false);
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

		public void loadFileInto(Ship ship, World world) {
			world.loadShip(f, ship);
		}

		

		public void set(FileHandle f) {
			this.f = f;
			setText(f.name());
			//setChecked(false);
		}
		
	}
	
	public static class WeaponButton extends TextButton{

		public ProgressBar slider;
		private int index;

		public WeaponButton(Skin skin, int i) {
			super("fjslk", skin);
			index = i;
			slider = new ProgressBar(0, 1, 0.01f, false, skin);
			slider.setFillParent(true);
			getLabel().setColor(Color.BLACK);
			//getCell(getLabel()).pad(0);
			//getCell(getLabel()).space(0);
			addActorBefore(getLabel(), slider);
			//getLabelCell().padTop(0);
			//slider.setHeight(getHeight());
			slider.getStyle().background.setMinHeight(getHeight());;
			
			slider.getStyle().knobBefore.setMinHeight(getHeight());;
			//slider.getStyle().knobAfter.setMinWidth(0);;
			//slider.getStyle().knobAfter.setRightWidth(0f);;
			slider.setColor(Color.GRAY);
			
			slider.setValue(0.5f);
		}
		@Override
		public void draw(Batch batch, float parentAlpha) {
			//slider.setHeight(getHeight());
			
			super.draw(batch, parentAlpha);
		}
	}
	public Entity getEntity() {
		return entity;
	}

	public void dispose() {
		skin.dispose();
		
	}
	public void resetShip() {
		set(ship);
		
	}
	public void makeQuestWindow(Skin skin) {
		questWindow = new Window("changeme", skin);
		questTextLabel = new Label("", skin);
	}
	
	public void showQuestScreen(GameInfo info, Stage stage, Ship ship, World world) {
		
		Planet planet = null;
		if (info.currentPlanet == -1) {
			planet = info.systems[info.currentSystem].sun;
			
		} else {
			planet = info.systems[info.currentSystem].planets[info.currentPlanet];
			
		}
		for (int i = 0; i < planet.quests.size; i++) {
			int questHash = planet.quests.get(i);
			Quest quest = info.getQuest(questHash);
			if (quest == null) throw new GdxRuntimeException("null quest" + questHash);
			if (!info.hasCompleted(questHash, info.currentSystem, info.currentPlanet) && quest.isValidForPlaying(planet, info.currentOrbit)) {
				showQuestScreen(info, stage, ship, quest, planet, world);
				break;
			}
		}
	}
	
	public void showQuestScreen(GameInfo info, Stage stage, Ship ship, String questName, World world) {
		Planet planet = info.systems[info.currentSystem].planets[info.currentPlanet];
		Quest quest = info.getQuest(questName);
		if (quest == null) throw new GdxRuntimeException("quest name does not exist");
		showQuestScreen(info, stage, ship, quest, planet, world);

	}
	
	public void showQuestScreen(GameInfo info, Stage stage, Ship ship, Quest quest, Planet planet, World world) {
		questWindow.getTitleLabel().setText(planet.toString());
		for (Actor a : questWindow.getChildren()) {
			if (a instanceof QuestOptionDisplay) {

				//questOptionPool.free((QuestOptionDisplay) a);
			}
		}
		questWindow.clearChildren();
		questTextLabel.setText(quest.text);
		questWindow.add(questTextLabel);
		questWindow.row();

		//Gdx.app.log(TAG, "ADD OPTIONS");
		if (quest.options.size == 0) {
			throw new GdxRuntimeException("ndskjfl");
		}
		{
			for (int k = 0; k < quest.options.size; k++) {
				if (!quest.options.get(k).isValid(info, planet, ship)){
					Gdx.app.log(TAG, "skip  OPTION TO WINDOW " + quest.options.get(k).text);
					continue;
				}
				QuestOptionDisplay opt = questOptionPool.obtain();
				opt.set(quest.options.get(k), info, ship, world);
				questWindow.add(opt);
				Gdx.app.log(TAG, "ADD OPTION TO WINDOW " + opt.getText());
				questWindow.row();
			}
		}
		if (quest.commands != null)
			for (int i = 0; i < quest.commands.length; i++){
				ship.doCommand(quest.commands[i], info, this, world, stage);
			}
		
		questWindow.pack();
		questWindow.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Align.center);
		stage.addActor(questWindow);
		questWindow.pack();
		Gdx.app.log(TAG, "DONE WINDOW" + quest.name + " " + quest.options.size);
	}
	public static class QuestOptionDisplay extends TextButton{

		private QuestOption questO;
		private GameInfo info;
		private UI ui;
		private Stage mstage;
		private Ship ship;
		private World world;

		public QuestOptionDisplay(String text, Skin skin, UI ui) {
			super(text, skin);
			this.ui = ui;
		}

		public void set(QuestOption questOption, GameInfo info, Ship ship, World world) {
			this.questO = questOption;
			this.world = world;
			setText(questOption.getText(info, ship));
			this.info = info;
			this.ship = ship;
		}

		public void selected() {
			if (questO.commands != null)
				for (int i = 0; i < questO.commands.length; i++){
					ship.doCommand(questO.commands[i], info, ui, world, mstage);
				}
			
			ui.clearQuestWindow();
			if (questO.next == null || questO.next.length == 0) {
				ui.closeQuestWindow();
				return;
			}
			if (questO == null) throw new GdxRuntimeException("null ");
			if (questO.next.length == 0) throw new GdxRuntimeException("null ");
			Gdx.app.log(TAG, "length " + questO.next.length + questO.text);
			for (int i = 0; i < questO.next.length; i++) {
				Quest q = info.getQuest(questO.next[i]);
				if (q != null) {
					Planet planet = null;
					if (info.currentPlanet == -1) {
						planet = info.systems[info.currentSystem].sun;
						
					} else {
						planet = info.systems[info.currentSystem].planets[info.currentPlanet];
						
					}
					Gdx.app.log(TAG, "OPEN NEW QUEST");
					
					ui.showQuestScreen(info, getStage(), ship, q, planet, world);
					
				}
				
			}
			
			
		}
		
	}
	public static class QuestOptionDisplayPool extends Pool<QuestOptionDisplay>{
		private Skin skin;
		private UI ui;
		public QuestOptionDisplayPool(Skin skin, UI ui) {
			this.skin = skin;
			this.ui = ui;
		}
		@Override
		protected QuestOptionDisplay newObject() {
			final QuestOptionDisplay d = new QuestOptionDisplay("fjsdklj", skin, ui);
			d.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					d.setChecked(false);
					
					d.selected();
					super.clicked(event, x, y);
				}
			});
			return d;
		}
		
	}
	public void clearQuestWindow() {
		//if (true) return;
		for (Actor a : questWindow.getChildren()) {
			if (a instanceof QuestOptionDisplay) {
				questOptionPool.free((QuestOptionDisplay) a);
			}
		}
		
	}
	public void closeQuestWindow() {
		questWindow.remove();
		
	}

	public class OrbitButton extends TextButton{
		public int cost;
		public OrbitButton(String text, Skin skin) {
			super(text, skin);
		}
	}
}