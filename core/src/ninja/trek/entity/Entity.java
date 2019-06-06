package ninja.trek.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

import ninja.trek.EntityAI;
import ninja.trek.MainSpaceCabal;
import ninja.trek.Ship;
import ninja.trek.World;
import ninja.trek.action.ActionList;
import ninja.trek.actions.ABase;
import ninja.trek.actions.ABreathe;

public class Entity implements Poolable {
	private static final String TAG = "entity";
	public static String[] raceNames = {"Human", "Reptiod", "Sloth", "Mechanoid", "System"};
	public static String[] jobNames = {"Shoot", "Fix", "[shield]Shield", "Wander", "[engine]Engine", "Fire", "[phaser]Weapon", "[drone]Drone", "[oxygen]Oxygen", "[teleporter]Teleport", "[science]Science"};
	public static final int SHOOT = 0;
	public static final int FIX = 1;
	public static final int SHIELDS = 2;
	public static final int WANDER = 3;
	public static final int ENGINE = 4;
	public static final int FIRE = 5;
	public static final int WEAPON = 6;
	public static final int DRONE= 7;
	public static final int OXYGEN= 8;
	public static final int TELEPORTER = 9;
	public static final int SCIENCE = 10;
	public static int RACE_HUMAN = 0, RACE_REPTOID = 1, RACE_SLOTH = 2, RACE_MECHANOID = 3, RACE_SYSTEM = 4;
	public int x, y;
	public String glyph = "A";

	public int font = 0;

	int[] ret = new int[2];
	public IntArray path;
	public int actionIndexForPath;
	public int[] speed = new int[jobNames.length];

	public transient Ship ship;
	public int[] buttonOrder = new int[jobNames.length];
	public boolean[] disabledButton = new boolean[jobNames.length];
	public transient GlyphLayout glyphLayout = new GlyphLayout()
			;
	private String iconName;
	public transient TextureAtlas.AtlasRegion icon;
	public Color iconColor = new Color(Color.WHITE);

	public void setIcon(String name) {
		iconName = name;
		if (name == null){
			icon = null;
			return;
		}
		//Gdx.app.log(TAG, "Icon change " + name);
		icon = MainSpaceCabal.iconAtlas.findRegion(name);
	}
	public void setIcon(String name, Color color) {
		setIcon(name);
		iconColor.set(color);
	}
	public void setIcon() {
		setIcon(iconName);
	}
	public void setIconColor(Color c){
		iconColor.set(c);
	}

	public enum ButtonType {DOOR_OPEN, DOOR_CLOSE, SHIELD_UP, SHIELD_DOWN };
	public ButtonType[] otherButtons = null;
	public int[] fixOrder = new int[FIX_ACTIONS_LENGTH];//block ids
	public static final int FIX_ACTIONS_LENGTH = 7;
	public GridPoint2 target = new GridPoint2();
	public int delayAccumulator;
	public boolean isHostile = false;;
	private transient ActionList ai;

	public Entity(){
		reset();
	}
	public Entity pos(int x, int y){
		this.x = x;
		this.y = y;
		return this;
	}
	public void update(World world, UI ui){
		if (ai == null){
			//Gdx.app.log(TAG, "no ai " + glyph);
			return;
		}
		ai.update(world, ship, ui);
	};
	public void handleOtherButton(ButtonType type) {

	}
	
	public Entity setAI(ActionList ai){
		resetAI();
		this.ai = ai;
		ai.setParent(this);
		return this;
	}

	public ActionList getAI(){
		return ai;
	}
	protected void resetAI() {
		if (this.ai != null){
			//Gdx.app.log(TAG, "non null ai");
			ai.clear();
			Pools.free(ai);
			ai = null;
		}
	}

	public Entity setMap(Ship map){
		this.ship = map;			
		return this;
	}

	@Override
	public void reset() {
		for (int i = 0; i < buttonOrder.length; i++){
			buttonOrder[i] = i;
		}
		delayAccumulator = 0;
		for (int i = 0; i < speed.length; i++){
			speed[i] = 90;
		}
		resetAI();
	}
	

	
	/**
	 * @param a value
	 * @param b value
	 */
	public int[] swap(int a, int b) {
		int aind = 0;
		for (int i = 0; i < buttonOrder.length; i++) if (buttonOrder[i] == a) aind = i;
		int bind = 0;
		for (int i = 0; i < buttonOrder.length; i++) if (buttonOrder[i] == b) bind = i;
		int c = buttonOrder[aind];
		//Gdx.app.log(TAG, "SWAP " + aind + "  " + bind);
		//Gdx.app.log(TAG, "SWVA " + buttonOrder[aind] + "  " + buttonOrder[bind]);
		buttonOrder[aind] = buttonOrder[bind];
		buttonOrder[bind] = c;
		ret[0] = aind;
		ret[1] = bind;
		updateFixOrder();
		return ret;
	}

	private void updateFixOrder() {
		int prog = 0;
		for (int i = 0; i < buttonOrder.length; i++){
			switch (buttonOrder[i]){
				case ENGINE:
					fixOrder[prog++] = Ship.ENGINE;
					break;
				case SHIELDS:
					fixOrder[prog++] = Ship.SHIELD;
					break;
				case OXYGEN:fixOrder[prog++] = Ship.OXYGEN;break;
				case DRONE:fixOrder[prog++] = Ship.DRONE;break;
				case WEAPON:fixOrder[prog++] = Ship.WEAPON;break;
				case TELEPORTER:fixOrder[prog++] = Ship.TELEPORTER;break;
				case SCIENCE:fixOrder[prog++] = Ship.SCIENCE;break;
			}
		}
	}

	public Entity setDefaultAI() {
		ActionList playerAction = new ActionList();
		playerAction.addToStart(Pools.obtain(ABase.class));
		playerAction.addToStart(Pools.obtain(ABreathe.class));

		setAI(playerAction);
		return this;
	}

	public void setDefaultButtonOrder() {
		buttonOrder[0] = FIRE;
		buttonOrder[1] = FIX;
		buttonOrder[2] = SHOOT;
		buttonOrder[3] = WEAPON;
		buttonOrder[4] = SHIELDS;
		buttonOrder[5] = DRONE;
		buttonOrder[6] = ENGINE;
		buttonOrder[7] = SCIENCE;
		buttonOrder[8] = OXYGEN;
		buttonOrder[9] = TELEPORTER;
		buttonOrder[10] = WANDER;
		updateFixOrder();
		
	}

	public Entity pos(GridPoint2 pos) {
		x = pos.x;
		y = pos.y;
		return this;
	}

	public void draw(SpriteBatch batch, OrthographicCamera camera, World world){};

	public void drawIcon(SpriteBatch batch, OrthographicCamera camera, World world, Array<TextureAtlas.AtlasRegion> icons) {
		if (icon == null) return;
		if (iconColor != null){
			batch.setColor(iconColor);
		}
			else batch.setColor(Color.WHITE);
		float w = 50 * camera.zoom, h = 50 * camera.zoom;
		batch.draw(icon, x-w/2, y - h/2, w, h);

	}

	public void drawBackground(SpriteBatch batch, OrthographicCamera camera, World world, Texture backgroundTexture) {
		if (icon == null) return;
		float w = 70 * camera.zoom, h = 70 * camera.zoom;
		batch.draw(backgroundTexture, x-w/2, y - h/2, w, h);
	}

}
