package ninja.trek;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import ninja.trek.Ship.Alignment;
import ninja.trek.entity.Entity;
import ninja.trek.gen.GameInfo;
import ninja.trek.ui.UISystemButton;

public class MainSpaceCabal extends ApplicationAdapter {
	private static final String TAG = "main";
	public static final String SHIP_SAVE_LOCATION = "SpaceCabal/ships/";
	public static final String QUEST_SAVE_LOCATION = "SpaceCabal/quests/";
	
	public static final String MAP_FILE_EXTENSION = "map";
	public static final String ENTITY_FILE_EXTENSION = "entities";
	public static final String MAP_PREVIEW_EXTENSION = ".png";
	
	public static final String HULL_SOURCE_FILES_LOCATION = "sources/";
	public static final String FONT_SAVE_LOCATION = "SpaceCabal/fonts/";
	public static final String MAP_HULL_EXTENSION = "png";
	public static final String MAP_BLOCKS_FILE_EXTENSION = "blk";
	public static final int THREADS = 1;
	public static final String MAP_INVENTORY_FILE_EXTENSION = "inv";
	private static final boolean PACK_QUESTS = !false;
	
	SpriteBatch batch;
	TextureAtlas atlas;
	OrthographicCamera camera;
	//private TextureAtlas uiAtlas;
	private FontManager fontManager;
	
	private World world;
	private ShaderProgram shader;
	private Stage stage;
	private UI ui;
	private IntMap<Vector2> touches = new IntMap<Vector2>();
	private IntIntMap touchButtons = new IntIntMap();
	private Vector3 v = new Vector3(), v2 = new Vector3();
	RayCaster ray = new RayCaster();
	private ShapeRenderer shape;
	private ScreenViewport viewport;
	private BackgroundRenderer background;
	private PlanetRenderer planet;
	private ModelBatch modelBatch;
	private Sprite pixelSprite;
	public static boolean paused;
	private ShipFactory shipFactory;
	private Texture backgroundTexture;
	public static TextureAtlas iconAtlas;
	private Array<TextureAtlas.AtlasRegion> icons;
	public int nextIconIndex;

	@Override
	public void create () {
		GLProfiler.enable();
		String[] args = {""};
		try {
			//WFC.makeOverlapping();
			//WFC.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CustomColors.init();
		Items.init();
		makeFonts();
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		if (Gdx.app.getType() == ApplicationType.Desktop && PACK_QUESTS) GameInfo.packQuests();
		viewport = new ScreenViewport();
		stage = new Stage(viewport );
		atlas = new TextureAtlas(Gdx.files.internal("background.atlas"));
		Sprites.init(atlas);
		//Sprite iconSprite = new Sprite(iconTexture);
		backgroundTexture = new Texture(Gdx.files.internal("circle.png"));
		iconAtlas = new TextureAtlas(Gdx.files.internal("icons.atlas"));
		icons = iconAtlas.getRegions();

		background = new BackgroundRenderer(atlas);
		modelBatch = new ModelBatch();;
		planet = new PlanetRenderer(6, 1f, modelBatch);
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);
		//camera.rotate(90);
		camera.update();
		InputMultiplexer mux = new InputMultiplexer();
		mux.addProcessor(stage);
		mux.addProcessor(new InputProcessor(){


			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Keys.A)
					world.getPlayerShip().map.boostAll();
				else if (keycode == Keys.TAB)
					ui.toggleInventory(world.getPlayerShip());
				else if (keycode == Keys.S)
					world.getPlayerShip().unReserveAll();
				else if (keycode == Keys.Z) {
					world.getEnemyShip().zoomingIn = true;
				}
				else if (keycode == Keys.X) {
					world.getEnemyShip().zoomingOut = true;
					
				} else if (keycode == Keys.SPACE) {
					paused = !paused;
				}
				else if (keycode == Keys.R) {
					//background.rotate( MathUtils.random(90, 300));
					background.rotate((MathUtils.random(0, 1) * 2 -1 ) * MathUtils.random(60, 120));
				} else if (keycode == Keys.C){
                    world.getPlayerShip().categorizeSystems();
                    world.getPlayerShip().calculateConnectivity(world);
					//Gdx.app.log(TAG, "recalc connectivity and systems");
                } else if (keycode == Keys.D){

				} else if (keycode == Keys.RIGHT){
					Entity e = ui.getEntity();
					if (e == null) return false;
					nextIconIndex++;
					if (nextIconIndex >= icons.size)
						nextIconIndex = 60 ;
					e.setIcon(icons.get(nextIconIndex).name);
				} else if (keycode == Keys.LEFT){
					Entity e = ui.getEntity();
					if (e == null) return false;
					nextIconIndex--;
					if (nextIconIndex < 0)
						nextIconIndex = icons.size-1;
					e.setIcon(icons.get(nextIconIndex).name);
				}
					
				
				return false;
			}

			@Override
			public boolean keyUp(int keycode) {
				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				return false;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				//Gdx.app.log(TAG, "touchdown");
				touches.put(pointer, Pools.obtain(Vector2.class).set(screenX, screenY));
				touchButtons.put(pointer, button);
				Ship ship = world.getPlayerShip();
				
				if (world.planetSelectOn){
					planet.click(screenX / (float)Gdx.graphics.getHeight(), screenY / (float)Gdx.graphics.getHeight(), ui);
					
					return true;
				}
				
				if (world.targettingIndex != -1){
					if (button != 0){
						//world.targettingIndex = -1;
						world.cancelTarget();
						return true;
					}
					Ship eShip = world.getEnemyShip();
					v.set(screenX, screenY, 0);
					eShip.camera.unproject(v);
					if (v.x < 0 || v.y < 0 || v.x >= eShip.mapWidth || v.y >= eShip.mapHeight){
						world.cancelTarget();
						return true;
					}
					//TODO unproject and check for vacuum
					if (eShip == null) throw new GdxRuntimeException("null ship");
					ship.setWeaponTarget(world.targettingIndex, (int)v.x, (int)v.y, eShip);
					ui.set(ship);
					eShip.zoomOutForTarget();
					world.targettingIndex = -1;
					return true;
				}

				if (ship.editMode && ship.placeDoor == true){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					ship.placeDoor = false;
					ship.placeDoor((int)v.x, (int)v.y, (int)ui.brushSizeSlider.getValue());
					//if (true) throw new GdxRuntimeException("placed door");
				} else if (ship.editMode && ship.deleteDoor == true){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					ship.deleteDoor = false;
					ship.deleteDoor((int)v.x, (int)v.y);
				} else if (ship.editMode && ship.placeSpawn == true){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					ship.placeSpawn = false;
					ship.placeSpawn((int)v.x, (int)v.y);
				}
				else if (ship.editMode && ship.deleteWeapon == true){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					ship.deleteWeapon = false;
					ship.deleteWeapon((int)v.x, (int)v.y);
				}
				else if (ship.editMode && ship.placeWeapon == true){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					v.x = Math.min(Math.max(v.x, 1), ship.mapWidth-2);
					v.y = Math.min(Math.max(v.y, 1), ship.mapHeight-2);
					ship.placeWeapon = false;
					ship.addWeapon((int)v.x, (int)v.y);
				}
				else if (ui.editLineButton.isChecked() && button == 0 && ship.editMode){
					ui.previewLine = true;
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					v.x = Math.min(Math.max(v.x, 1), ship.mapWidth-2);
					v.y = Math.min(Math.max(v.y, 1), ship.mapHeight-2);
					Vector2 prev = touches.get(pointer);
					v2.set(prev.x, prev.y, 0);
					ship.camera.unproject(v2);
					v2.x = Math.min(Math.max(v2.x, 1), ship.mapWidth-2);
					v2.y = Math.min(Math.max(v2.y, 1), ship.mapHeight-2);
					ui.setPreviewLine(v, v2);
				} else
				if (ship.editMode && button == 0){
					if (ui.randomFillButton.isChecked() && !ui.editLineButton.isChecked()){
						v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
						ship.camera.unproject(v);
						UISystemButton btn = (UISystemButton) ui.lastPressedShipSystemButton;
						int block = btn.index;
						int b = ship.map.get((int)v.x, (int)v.y);
						ship.fill.resetRandomFloodFill(200, 250);
						//block = MathUtils.random(1, 3);
						ship.fill.randomFloodFill(ship.map, (int)v.x, (int)v.y
								, block, MathUtils.random(20000, 100000), false, ship);
						ship.fill.clear();
						//ship.setAllDirty();
						//ship.setDirty((int)v.x, (int)v.y);
					} else
					if (ui.fillBtn.isChecked() && !ui.editLineButton.isChecked()){
						v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
						ship.camera.unproject(v);
						UISystemButton btn = (UISystemButton) ui.lastPressedShipSystemButton;
						int block = btn.index;
						int b = ship.map.get((int)v.x, (int)v.y);
						ship.map.floodFill(ship.map, (int)v.x, (int)v.y
								, b & Ship.BLOCK_ID_MASK, block);
						//ship.setAllDirty();
						//ship.setDirty((int)v.x, (int)v.y);
					} else {
						v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
						ship.camera.unproject(v);
						v.x = Math.min(Math.max(v.x, 1), ship.mapWidth-2);
						v.y = Math.min(Math.max(v.y, 1), ship.mapHeight-2);
						UISystemButton btn = (UISystemButton) ui.lastPressedShipSystemButton;
						int block = btn.index;
						if (block == 0) block = 0b1000000000000000000000000000000;
						if (ui.fireBtn.isChecked()){
							block = ship.map.get((int)v.x, (int)v.y);
							block |= (1 << Ship.BLOCK_FIRE_BITS);
							//block |= (63 << Ship.BLOCK_AIR_BITS);
							//Gdx.app.log(TAG, "FIRE" + block);
						}
						if (block == Ship.VACCUUM){
							block |= (2 << Ship.BLOCK_FIRE_BITS);
						}
						//Gdx.app.log(TAG, "set " + block);
						ship.fill.setRect((int)v.x, (int)v.y, block, (int)ui.brushSizeSlider.getValue(), ship);
						//ship.map.set((int)v.x, (int)v.y, block);
						//ship.setDirty((int)v.x, (int)v.y);
						if (ui.xMirrorBtn.isChecked()){
							ship.fill.setRect(ship.mapWidth - 1 - (int)v.x, (int)v.y, block, (int)ui.brushSizeSlider.getValue(), ship);
	//						ship.map.set(ship.mapWidth - 1 - (int)v.x, (int)v.y, block);
//							ship.setDirty(ship.mapWidth - 1 - (int)v.x, (int)v.y);
						}
					} 
				} else if (button == 0){
					
					v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
					v2.set(Gdx.input.getX(), Gdx.input.getY(), 0);
					ship.camera.unproject(v);
					Ship shipB = world.getEnemyShip();
					shipB.camera.unproject(v2);
					
					ship.selectClosestEntity((int)v.x, (int)v.y, ui, shipB, (int)v2.x, (int)v2.y);
					//Gdx.app.log(TAG, "ADD WEAPON");
				}
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				Ship ship = world.getPlayerShip();

				if (ship.editMode && ui.editLineButton.isChecked() && button == 0){
					ui.previewLine = false;
					int s = (int) ui.brushSizeSlider.getValue();
					int h = s/2;
					int dx =  (int) ui.previewA.x - (int) ui.previewB.x;
					int dy =  (int) ui.previewA.y - (int) ui.previewB.y;
					//dx = -dx;
					//dy = -dy;
					if (Math.abs(dx) > Math.abs(dy)){
						dy = 0;
					} else {
						dx = 0;
					}
					v.set(ui.previewB.x, ui.previewB.y, 0);

					UISystemButton btn = (UISystemButton) ui.lastPressedShipSystemButton;
					int block = btn.index;
					if (block == 0) block = 0b1000000000000000000000000000000;

					int x0 = (int) ui.previewB.x;
					int y0 = (int) ui.previewB.y;
					int x1 = (int) ui.previewB.x + dx;
					int y1 = (int) ui.previewB.y + dy;
					if (x0 > x1){
						int t = x0;
						x0 = x1;
						x1 = t;
					}
					if (y0 > y1){
						int t = y0;
						y0 = y1;
						y1 = t;
					}
					int ddx = 0;
					if (s % 2 == 0){
						ddx = 1;
					}
					for (int x = x0 ; x <= x1; x++)
						for (int y = y0; y <= y1; y++){
							ship.fill.setRect(x, y, block, (int)ui.brushSizeSlider.getValue(), ship);
							if (ui.xMirrorBtn.isChecked())
								ship.fill.setRect(ship.mapWidth - 1 - x + ddx,  y, block, (int)ui.brushSizeSlider.getValue(), ship);
						}

				}
				
				if (ship.editMode){
					ship.map.overWriteFrom(ship.fill);
					ship.fill.clear();
					//ship.setAllDirty();
				}
				ship.setRedrawFill();
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				//Gdx.app.log(TAG, "dragged " + screenX + "  " + screenY);
				Ship ship = world.getPlayerShip();
				if (ship.editMode && touchButtons.get(pointer, 3) == 0 && !ui.fillBtn.isChecked() && !ui.randomFillButton.isChecked() && !ui.editLineButton.isChecked()){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					v.x = Math.min(Math.max(v.x, 1), ship.mapWidth-2);
					v.y = Math.min(Math.max(v.y, 1), ship.mapHeight-2);
					Vector2 prev = touches.get(pointer);
					v2.set(prev.x, prev.y, 0);
					ship.camera.unproject(v2);
					v2.x = Math.min(Math.max(v2.x, 1), ship.mapWidth-2);
					v2.y = Math.min(Math.max(v2.y, 1), ship.mapHeight-2);					
					//Gdx.app.log(TAG, "set " + v + v2);
					ray.trace(v2.x, v2.y, v.x, v.y);
					while (ray.hasNext){
						ray.next();
						if (ray.hasNext){//skip last block
							//if (ship.map.get(ray.x, ray.y) == 1) Gdx.app.log(TAG, "DUPE");
							UISystemButton btn = (UISystemButton) ui.lastPressedShipSystemButton;
							int block = btn.index;
							if (block == 0) block = 0b1000000000000000000000000000000;
							if (ui.fireBtn.isChecked()){
								block = ship.map.get(ray.x, ray.y);

								block |= (1 << Ship.BLOCK_FIRE_BITS);
								//block |= (63 << Ship.BLOCK_AIR_BITS);
								//Gdx.app.log(TAG, "FIRE" + block);
							}
							//ship.map.set(ray.x, ray.y, block);
							ship.fill.setRect(ray.x, ray.y, block, (int)ui.brushSizeSlider.getValue(), ship);
							
							//ship.setDirty(ray.x, ray.y);
							if (ui.xMirrorBtn.isChecked()){
								//ship.map.set(ship.mapWidth - 1 - ray.x, ray.y, block);
								ship.fill.setRect(ship.mapWidth - 1 - ray.x, ray.y, block, (int)ui.brushSizeSlider.getValue(), ship);
								//ship.setDirty(ship.mapWidth - 1 - ray.x, ray.y);
							}
							//Gdx.app.log(TAG, "set " + block);
						}
					}
					prev.set(screenX, screenY);
				} else if (ui.destroyButton.isChecked() && ship.editMode){
					
					
				}else if (ui.editLineButton.isChecked() && ui.previewLine && ship.editMode){
					v.set(screenX, screenY, 0);
					ship.camera.unproject(v);
					v.x = Math.min(Math.max(v.x, 1), ship.mapWidth-2);
					v.y = Math.min(Math.max(v.y, 1), ship.mapHeight-2);
					Vector2 prev = touches.get(pointer);
					v2.set(prev.x, prev.y, 0);
					ship.camera.unproject(v2);
					v2.x = Math.min(Math.max(v2.x, 1), ship.mapWidth-2);
					v2.y = Math.min(Math.max(v2.y, 1), ship.mapHeight-2);
					ui.setPreviewLine(v, v2);
					
				}
				else {
					Vector2 current = touches.get(pointer);
					float dx = current.x - screenX;
					float dy = screenY - current.y;
					Ship map = world.getMapForDragging(Gdx.input.getX(), Gdx.input.getY());
					map.drag(dx, dy);
					current.set(screenX, screenY);
					
				}
				ship.setRedrawFill();
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				Ship map = world.getMapForScreenPosition(Gdx.input.getX(), Gdx.input.getY());
				//map = world.getPlayerShip();
				if (map != null){
					v.set(Gdx.input.getX(), Gdx.input.getY(), 0);
					map.camera.unproject(v);
					//Gdx.app.log(TAG, "unprojected  " + v);

					if (map.alignment == Alignment.CENTRE){
						map.camera.zoom *= amount<0? 0.9f : 1f/0.9f;
						map.updateCamera(camera, world, 0, false);
						map.camera.project(v);
						//Gdx.app.log(TAG, "projected" + v);
						v2.set(Gdx.input.getX(), Gdx.graphics.getHeight() -1 - Gdx.input.getY(), 0);
						v2.sub(v);
						v2.scl(map.camera.zoom);
						
						map.offsetForZoom(v2.x, v2.y);
						
					} else if (map.alignment == Alignment.TOP_RIGHT){
						map.zoomedOutEnemyZoom*= amount<0? 0.9f : 1f/0.9f;
					}
				}
				return true;
			}
		});

		Gdx.input.setInputProcessor(mux);
		
		//uiAtlas = new TextureAtlas(Gdx.files.internal("ui/ui.atlas"));
		TextureAtlas fontAtlas = new TextureAtlas("ui/ui.atlas");
		fontManager = new FontManager(fontAtlas);
		pixelSprite = new Sprite(new Texture("pixel.png"));

		shader = new ShaderProgram(Gdx.files.internal("lighting.vert"), Gdx.files.internal("lighting.frag"));
		if (!shader.isCompiled()) throw new GdxRuntimeException("shader \n"  + shader.getLog());

		shipFactory = new ShipFactory(pixelSprite, fontManager, shader);


		world = new World(fontManager, shader, pixelSprite, planet, modelBatch, shipFactory, iconAtlas, batch);
		
		if (!Gdx.files.internal("lighting.vert").exists()) throw new GdxRuntimeException("kdls");

		
		float[] colorArray = CustomColors.getFloatColorArray();
		world.colorIndexShader.begin();
		//Gdx.app.log(TAG, "shader " + shader.getUniformLocation("u_colors[0]") + shader.isCompiled() + shader.getLog());
		world.colorIndexShader.setUniform4fv("u_colors[0]", colorArray, 0, colorArray.length);
		
		world.colorIndexShader.end();
		
		batch.setShader(shader);
		Ship amap = shipFactory.createShip();
		world.addMap(amap);
		amap.inventory.add(Items.laser1);
		amap.inventory.add(Items.rocket2);
		
		Ship amap2 = shipFactory.createShip();
		world.addMap(amap2);
		
//		Entity player = new Entity().pos(0, 0).setAI(playerAction );
		//Entity player = new Entity().pos(0, 0).setDefaultAI();
		//amap.addEntity(player);
		
		//amap2.addEntity(new Entity().pos(30,30).setAI(new FungusAI()));
		//amap2.offset.set(200, 200);
		//amap2.offsetSize.set(200, 200);
		amap2.alignment = Alignment.TOP_RIGHT;
		
		ui = new UI(stage, world, fontManager, iconAtlas, background);
		ui.setEntity(null);
		ui.set(null);//world.getPlayerShip());
	}

	@Override
	public void render () {
		CustomColors.updateColors(Gdx.graphics.getDeltaTime(), world.colorIndexShader);
		//stage.setDebugAll(true);
		stage.act(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (!paused)
			world.update(batch, camera, ui, background, planet, stage);
		
		//batch.begin();
		//batch.draw(img, 0, 0);
		//background.draw(world, paused);
		planet.draw(batch, shape, paused, pixelSprite, background.rotation, ui.showBackgroundPlanet.isChecked());
		world.draw(batch, camera, shape, ui, paused, icons, backgroundTexture);
		//batch.end();
		//stage.getBatch().disableBlending();
		stage.draw();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2.1f);
		batch.setShader(null);
		batch.begin();
		Texture tex = world.colorIndexBuffer.getColorBufferTexture();
		//if (world.getPlayerShip() != null)
			//Gdx.app.log(TAG, ""+ "  " + world.getPlayerShip().mapWidth);
		Gdx.app.log(TAG, ""+GLProfiler.textureBindings + "  " + GLProfiler.drawCalls );
		GLProfiler.textureBindings = 0;
		GLProfiler.drawCalls = 0;
		//batch.draw(tex, .005f, 0, 2f, 2f);
		batch.end();
	}
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		camera.setToOrtho(false, width, height);
		ui.resize();
		//viewport.apply();
		//stage.setViewport(viewport);
		//ui.table.invalidate();
		background.resize(width, height);
		super.resize(width, height);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		atlas.dispose();
		fontManager.dispose();
		iconAtlas.dispose();
		shader.dispose();
		stage.dispose();
		ui.dispose();
		shape.dispose();
		background.dispose();
		planet.dispose();
		world.dispose();
	}
	
	public void makeFonts(){
		
	}
}
