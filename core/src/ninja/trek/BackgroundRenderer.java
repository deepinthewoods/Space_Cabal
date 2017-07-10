package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ninja.trek.Ship.Alignment;

public class BackgroundRenderer {
	private static final int MAX = 1000;

	private static final float STAR_SPEED = 0.05f;
	
	private Vector3[] stars = new Vector3[300];

	private SpriteBatch batch;

	private Sprite[] sprite;

	private OrthographicCamera cam;

	
	public BackgroundRenderer(TextureAtlas atlas) {
		for (int i = 0; i < stars.length; i++){
			stars[i] = new Vector3(
					MathUtils.random(MAX)
					, MathUtils.random(MAX)
					, MathUtils.random(MAX)
					);
		}
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.translate(MAX/2,  MAX/2);
		batch = new SpriteBatch();
		sprite = new Sprite[]{atlas.createSprite("smallstar"), atlas.createSprite("smallstar"), 
				atlas.createSprite("smallstar"), atlas.createSprite("smallstar"),
				atlas.createSprite("smallstar"), atlas.createSprite("smallstar"),
				atlas.createSprite("medstar"), atlas.createSprite("bigstar")
		};
	}
	Vector3 v, v3 = new Vector3();
	Vector2 move = new Vector2(), mv = new Vector2();
	public void setAlpha(float alpha){
		this.alpha = Math.max(Math.min(alpha, 1f), 0f);
	}
	private float widthMod = 1f, maxWidth = 1000f, alpha = 0f;
	public void draw(World world, boolean paused) {
		widthMod = MathUtils.lerp(1f, maxWidth, alpha);
		//if (true)return;
		Alignment alignment = Alignment.CENTRE;
		if (alignment  == Alignment.CENTRE){
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			v3.set(-Ship.GAP, 0, 0);
			Ship otherShip = world.getEnemyShip();
			otherShip.camera.project(v3);
		
			int width = (int) v3.x;
			Gdx.gl.glScissor(0, 0, width, Gdx.graphics.getHeight());
		}
		//batch.disableBlending();
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		if (paused)
			move.set(0, 0);
		else move.set(1, 0).scl(Gdx.graphics.getDeltaTime() * STAR_SPEED);
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		for (int i = 0; i < stars.length; i++){
			v = stars[i];
			mv.set(move);
			mv.scl(v.z);
			v.add(mv.x, mv.y, 0);
			v.x = (((v.x % MAX) + MAX) % MAX);
			v.y = (((v.y % MAX) + MAX) % MAX);
			Sprite sprit = sprite[i%8];
			//batch.draw(sprite[i%8], v.x, v.y);
			sprit.setCenter(v.x, v.y);
			sprit.setSize(sprit.getRegionWidth() * widthMod, sprit.getRegionHeight());
			sprit.draw(batch);
		}
		batch.end();
		//cam.rotate(1f, 0f, 0f, 1f);
		cam.update();
		if (alignment == Alignment.CENTRE){
			Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
			
		}
		
	}
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
