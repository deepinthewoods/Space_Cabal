package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import squidpony.squidgrid.AestheticDifference;

public class OuterHull {

	private static final String TAG = "outer hull";

	private DetailedMimicPartial mim;

	private Texture region;
	
	public OuterHull(){
		AestheticDifference diff = new AestheticDifference() {
			
			@Override
			public double difference(int a, int b) {
				
				return 0;
			}
		};
		mim = new DetailedMimicPartial(null);
	}
	
	public void calculate(Ship ship, String name, int reps, int radius){
		Pixmap pix = new Pixmap(Gdx.files.internal("sources/"+name));
		
		float[] priorities = new float[ship.mapWidth * ship.mapHeight];
		int[] pixels = new int[pix.getWidth() * pix.getHeight()];
		
		for (int i = 0; i < pix.getWidth(); i ++)
			for (int k = 0; k < pix.getHeight(); k++){
				pixels[i + k * pix.getWidth()] = pix.getPixel(i, k);
			}
		float[] blackProb = new float[ship.mapWidth * ship.mapHeight];
		for (int i = 0; i < ship.mapWidth; i ++)
			for (int k = 0; k < ship.mapHeight; k++){
				
				blackProb[i + k * ship.mapWidth] = 1f;
				float alpha = 0f;
				if (i < 50){
					//alpha = .5f;
					
				} else if (i < 60){
					alpha = 1f;
				} else if (i < 150){
					alpha = 1.2f;
				} 
				
				//alpha = Math.min(Math.max(0f,  alpha),  1f);
				//if (alpha > 0)Gdx.app.log(TAG, "alpha " + alpha);
				blackProb[i + k * ship.mapWidth] = alpha ;
				//blackProb[i + k * ship.mapWidth] = 10f;
			}
		int[] result = mim.neoProcess(pixels, pix.getWidth(), pix.getHeight(), ship.mapWidth, ship.mapHeight, reps, radius, false, blackProb);
		Pixmap npix = new Pixmap(ship.mapWidth, ship.mapHeight, Format.RGBA8888); 
		for (int i = 0; i < ship.mapWidth; i ++)
			for (int k = 0; k < ship.mapHeight; k++){
				npix.drawPixel(i, k, result[i + k * ship.mapWidth]);
			}
		region = new Texture(npix);
	}

	public void draw(SpriteBatch batch, OrthographicCamera camera, World world, Ship ship) {
		batch.setShader(null);
		batch.begin();
		if (region != null) batch.draw(region, 0, 0, ship.mapWidth, ship.mapHeight);
		batch.end();
	}
}
