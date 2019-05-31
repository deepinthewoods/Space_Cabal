package ninja.trek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.AtomicQueue;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

import ninja.trek.gen.GameInfo;
import ninja.trek.gen.Planet;
import ninja.trek.gen.SolarSystem;
import squidpony.squidgrid.gui.gdx.SColor;


 
/**
 * Approximates a sphere as a simplicial polyhedron, formed by subdividing the triangles of a regular icosahedron into
 * smaller triangles. Normally used to achieve a more isotropical layout of vertices than a UV sphere.
 *
 * @author jayfella
 */
 
// to see inside, change indices from 1-2-3 to 1-3-2.
public class PlanetRenderer implements RenderableProvider{
 
    private static final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
 
    private static final boolean MAKE_TEXTURE = true;

	private static final int VERTEX_TOTAL_FLOATS = MAKE_TEXTURE?9:7;

	private static final String TAG = "planet renderer ";

	private static final float ROTATION_SPEED = 8;

	private static final float SELECTED_ROTATION_SPEED = 140;
	
	private static final int BUFFER_SIZE = 350;

	private static final int MAX_PLANET_VERT_ARRAYS = SolarSystem.MAX_PLANETS_PER_SYSTEM + SolarSystem.OTHER_BODIES_PER_SYSTEM;

	private static final float SELECTED_PLANET_ZOOM_SPEED = 3f;

	private static final int TOTAL_PLANET_COLOR_SETS = 12;

	private static final Color[] GAS_GIANT_COLOR = {SColor.TAN, SColor.DB_IRON, Color.CYAN, SColor.GOLDEN_YELLOW};
 
	private static final Color[] INNER_PLANET_COLOR = {SColor.LAVENDER_GRAY, SColor.LIGHT_GRAY, SColor.ORANGE, SColor.ATOMIC_TANGERINE};
	
	

	private static final float SUN_ROTATE_SPEED = 40;

	private static final int TEXTURE_WIDTH = 300;

	private static final int TEXTURE_HEIGHT = 200;
	
    private Map<Long, Integer> middlePointIndexCache = new HashMap<>();
    
    private List<Vector3> vertices = new ArrayList<>();

	private Environment environment;

	private ModelBatch modelBatch;

	private PerspectiveCamera cam;

	private Mesh mesh;

	private float[][] verts;

	//private Texture texture;


	private short[] faceArray;

	private Icosphere sphere;

	private Pixmap pixmap;
	
	private float[][][] colors;
	
	private Color[][][] colorsC;
	
	private Vector3 lightADirection = new Vector3(), lightBDirection = new Vector3();

	private DirectionalLight lightA;

	private DirectionalLight lightB;
	
	private Color lightAColor = new Color(Color.WHITE), lightBColor = new Color(Color.DARK_GRAY);
	
	//float seed = MathUtils.random();
    
	Vector3 v = new Vector3();
	
    SimplexNoise noise = new SimplexNoise();

	private float orbitSpeed = 1f;

	private FrameBuffer[] buffer = new FrameBuffer[MAX_PLANET_VERT_ARRAYS];
	
	private Sprite[] sprite = new Sprite[MAX_PLANET_VERT_ARRAYS];
	
	private float[] toX = new float[MAX_PLANET_VERT_ARRAYS], toY = new float[MAX_PLANET_VERT_ARRAYS];

	private float[] sizeModifier = new float[MAX_PLANET_VERT_ARRAYS];

	//private FloatArray[] oceanVerts;

	//private ShortArray[] oceanIndices;

	//private Mesh oceanMesh;
	
	private IntArray lerpingIn = new IntArray(), lerpingOut = new IntArray();

	private Icosphere baseSphere;
 
	Color c = new Color();

	private PauseableThread[] threads;

	private Material[] materials;

	private Texture[] planetTextures;

	private FrameBuffer[] surfaceTextureBuffer;

	//private Pixmap[] planetPix;


	
    public PlanetRenderer(int recursionLevel, float size, ModelBatch batch) {
    	for (int i = 0; i < sizeModifier.length; i++)
    		sizeModifier[i] = 1f;
    	
    	//texture = new Texture(Gdx.files.internal("biomes.png"));
    	
    	pixmap = new Pixmap(Gdx.files.internal("biomes.png"));
    	colors = new float[TOTAL_PLANET_COLOR_SETS][pixmap.getWidth()][pixmap.getHeight()];
    	colorsC = new Color[TOTAL_PLANET_COLOR_SETS][pixmap.getWidth()][pixmap.getHeight()];

    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			int p = pixmap.getPixel(i, k);
    			c.set(p);
    			colors[0][i][k] = c.toFloatBits();
    			colorsC[0][i][k] = new Color(c);
    		}
    	}
    	pixmap.dispose();
    	pixmap = new Pixmap(Gdx.files.internal("biomes2.png"));
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			int p = pixmap.getPixel(i, k);
    			c.set(p);
    			colors[1][i][k] = c.toFloatBits();
    			colorsC[1][i][k] = new Color(c);
    		}
    	}
    	pixmap.dispose();
    	pixmap = new Pixmap(Gdx.files.internal("biomes3.png"));
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			int p = pixmap.getPixel(i, k);
    			c.set(p);
    			colors[2][i][k] = c.toFloatBits();
    			colorsC[2][i][k] = new Color(c);
    		}
    	}
    	pixmap.dispose();
    	pixmap = new Pixmap(Gdx.files.internal("biomes4.png"));
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			int p = pixmap.getPixel(i, k);
    			c.set(p);
    			colors[3][i][k] = c.toFloatBits();
    			colorsC[3][i][k] = new Color(c);
    		}
    	}
    	pixmap.dispose();
    	for (int gasGiant = 4; gasGiant < 8; gasGiant++)
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			//int p = pixmap.getPixel(i, k);
    			c.set(GAS_GIANT_COLOR[gasGiant-4]);
    			alpha = MathUtils.random() * .2f;
    			c.lerp(0f,  0f, 0f, 1f, alpha);
    			colors[gasGiant][i][k] = c.toFloatBits();
    			colorsC[gasGiant][i][k] = new Color(c);
    		}
    	}
    	for (int innerPlanet = 8; innerPlanet < 12; innerPlanet++)
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			//int p = pixmap.getPixel(i, k);
    			c.set(INNER_PLANET_COLOR[innerPlanet-8]);
    			alpha = MathUtils.random() * .2f;
    			c.lerp(0f,  0f, 0f, 1f, alpha);
    			colors[innerPlanet][i][k] = c.toFloatBits();
    			colorsC[innerPlanet][i][k] = new Color(c);
    		}
    	}
    	//colors[0][0].set(pixmap.getPixel(pixmap.getWidth()-1,  pixmap.getHeight()-1));
    	//colors[0][0].set(pixmap.getPixel(0, 0));
    	//Gdx.app.log("planet", "pixel " + colors[0][0].r + " " + colors[0][0].g + " " + colors[0][0].b + " " + pixmap.getWidth());
    	//texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    	
    	
    
    	
    	
    	
    	baseSphere = new Icosphere(1, 1, 8f);
    	sphere = new Icosphere(baseSphere);
    	sphere.initTwins();
    	
        environment = new Environment();
		lightA = new DirectionalLight();
		lightA.set(Color.WHITE, lightADirection);
		environment.add(lightA);
		
		lightB = new DirectionalLight();
		lightB.set(Color.WHITE, lightBDirection);
		environment.add(lightB);
        	
		modelBatch = batch;

		cam = new PerspectiveCamera(60, BUFFER_SIZE, BUFFER_SIZE);
		cam.near = 10;
		cam.far = 40;
		cam.position.set(0f, 20f, 0f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		//Gdx.app.log("sphere", "type " + sphere.vertices.toArray().getClass());
		
		//spherePointArray = sphere.vertices.toArray(Vector3.class);
		faceArray = new short[sphere.indices.size];
		if (MAKE_TEXTURE) {			
			mesh = new Mesh(true, sphere.vertices.size * VERTEX_TOTAL_FLOATS, faceArray.length, 
					VertexAttribute.Position()
					, VertexAttribute.ColorPacked()
					, VertexAttribute.Normal()
					, VertexAttribute.TexCoords(0)
					);
		} else {
			mesh = new Mesh(true, sphere.vertices.size * VERTEX_TOTAL_FLOATS, faceArray.length, 
					VertexAttribute.Position()
					, VertexAttribute.ColorPacked()
					//, VertexAttribute.TexCoords(0)
					, VertexAttribute.Normal()
					);
		}
		/*oceanMesh = new Mesh(true, spherePointArray.length * VERTEX_TOTAL_FLOATS, faceArray.length * 2, 
				VertexAttribute.Position()
				, VertexAttribute.ColorPacked()
				//, VertexAttribute.TexCoords(0)
				, VertexAttribute.Normal()
				);*/
		
		
		planetTextures = new Texture[MAX_PLANET_VERT_ARRAYS];
		//(pix);
		surfaceTextureBuffer = new FrameBuffer[MAX_PLANET_VERT_ARRAYS];
		
		materials = new Material[MAX_PLANET_VERT_ARRAYS];
		for (int i = 0; i < materials.length; i++) {
			surfaceTextureBuffer[i] = new FrameBuffer(Format.RGBA8888, TEXTURE_WIDTH, TEXTURE_HEIGHT, false);
			planetTextures[i] = surfaceTextureBuffer[i].getColorBufferTexture();//new Texture(TEXTURE_WIDTH, TEXTURE_HEIGHT, Format.RGBA8888);
			
			materials[i] = new Material();
			if (MAKE_TEXTURE) {
				
				materials[i].set(TextureAttribute.createDiffuse(planetTextures[i]));
				//materials[i].set(TextureAttribute.create);
				//planetTextures[i].draw(planetPix[0], 0, 0);
			}
		}
		//material.set(TextureAttribute.createDiffuse(texture));
		//material.set(TextureAttribute.createAmbient(texture));
		//material.set(new ColorAttribute(ColorAttribute.createAmbient(Color.WHITE)));
		//material.set(new ColorAttribute(ColorAttribute.AmbientLight));
		
        verts = new float[MAX_PLANET_VERT_ARRAYS][];
        /*oceanVerts = new FloatArray[MAX_PLANET_VERT_ARRAYS];
        oceanIndices = new ShortArray[MAX_PLANET_VERT_ARRAYS];
        oceanVerts[0] = new FloatArray();
        oceanIndices[0] = new ShortArray();
        vectorsToVerts(verts[0], spherePointArray, oceanVerts[0], oceanIndices[0], sphere.indices);
         */
        for (int i = 0; i < buffer.length; i++) {
			buffer[i] = new FrameBuffer(Format.RGBA4444, BUFFER_SIZE, BUFFER_SIZE, false);
			sprite[i] = new Sprite(buffer[i].getColorBufferTexture());
			verts[i] = new float[sphere.vertices.size * VERTEX_TOTAL_FLOATS];
    	}
        
        faceArray =  sphere.indices.toArray();
        
        mesh.setIndices(faceArray);
        
        /*
        oceanMesh.setVertices(oceanVerts[0].toArray());
		oceanMesh.setIndices(oceanIndices[0].toArray());
         * FloatBuffer pb = BufferUtils.createFloatBuffer(vertexArray);
        setBuffer(VertexBuffer.Type.Position, 3, pb);
 
        IntBuffer ib = BufferUtils.createIntBuffer(faceArray);
        setBuffer(VertexBuffer.Type.Index, 3, ib);
 
        FloatBuffer tb = BufferUtils.createFloatBuffer(texArray);
        setBuffer(VertexBuffer.Type.TexCoord, 2, tb);
 
        updateBound();*/
        for (int i = 0; i < buffer.length; i++){
        	buffer[i] = new FrameBuffer(Format.RGBA4444, BUFFER_SIZE, BUFFER_SIZE, true);
        	buffer[i].getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        	sprite[i] = new Sprite(buffer[i].getColorBufferTexture());
        }
        threads = new PauseableThread[1];
        threads[0] = new PauseableThread(new CachePlanetRunnable(this));
        threads[0].start();
    }
    
    private void vectorsToVerts(float[] verts, Array<Vector3> vertices2, ShortArray oceanIndices, Planet planet, FloatArray uvs) {
    	int p = 0, i = 0, k = 0;
    	//float lowestR = 2f, highestR = 0f;
    	//float oceanColor = OCEAN_COLORS[planet.oceanColor], beachColor = BEACH_COLORS[planet.beachColor];
    	while ( p < verts.length){
    		//Gdx.app.log(TAG, "did " + vertexArray[i]);
    		Vector3 pos = vertices2.get(i);
    		pos.nor().scl(7.5f);
    		//pos.scl(1 + height);
    		v.set(pos).nor();
    		float height = noise.scaled(v.x, v.y, v.z, planet.seed / 100002f, 10 * (planet.landScale + .02f));
    		float oceanThreshold = planet.oceanHeight * 2 - 1;
    		//if (height > oceanThreshold){
    			if (planet.exponentialHeightScaling)
    				pos.scl(height * height * planet.height + 1);
    			else
    				pos.scl(height * .5f * planet.height + 1);
    		//}
    		
    		verts[p++] = pos.y;
    		verts[p++] = pos.x;
    		verts[p++] = pos.z;
    		pos.nor().scl(7.5f);
    		v.set(pos);
    		v.nor();
    		
   			//verts[p++] = lookupColor(1f-temp, 1f-rainfall, height, planet);
    		verts[p++] = Color.WHITE.toFloatBits();
    		
    		//verts[p++] = 1f - temp;
    		//verts[p++] = 1f - rainfall;

    		//v.set(0, 0, 0);
    				
    		
    		verts[p++] = v.x;    	
    		verts[p++] = v.y;    	
    		verts[p++] = v.z;   
    		
    		if (MAKE_TEXTURE) {
    			//Gdx.app.log(TAG, "uvindex " +  i + "  " + uvs.size + "  vs " + vertices2.size);
    			float uu = uvs.get(i*2);
    			float vv = uvs.get(i*2+1);
    			verts[p++] = uu;    	
        		verts[p++] = vv;    	
        		
    		}
    		//if (!done) Gdx.app.log(TAG, "jskl " +  i);
    		///WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    		//SDKFAAAAAAAAAAAAAAAAAAAAAAAA
    		i++;
    	}
    	
    }

	private float lookupColorf(float temp, float rainfall, float height, Planet planet) {
		
		int tempI = (int) (Math.min(Math.max(temp, 0f), 1f) * (colors[0].length-1));
		int rainfallI = (int) (Math.min(Math.max(temp, 0f), 1f) * (colors[0][0].length-1));
		//tempI = colors.length-3;
		//rainfallI = colors.length-3;
		//tempI = 63;
		//rainfallI = 63;
		//tempI = 0;
		//rainfallI = 0;
		//return colors[0][0].toFloatBits();
		//Gdx.app.log(TAG, "color " + temp + " " +  "  " + tempI);
		return colors[planet.colorIndex][tempI][rainfallI];
		
	}

	public Color lookupColor(float temp, float rainfall, float height, Planet planet) {
		
		int tempI = (int) (Math.min(Math.max(temp, 0f), 1f) * (colors[0].length-1));
		int rainfallI = (int) (Math.min(Math.max(rainfall, 0f), 1f) * (colors[0][0].length-1));
		//tempI = colors.length-3;
		//rainfallI = colors.length-3;
		//tempI = 63;
		//rainfallI = 63;
		//tempI = 0;
		//rainfallI = 0;
		//return colors[0][0].toFloatBits();
		//Gdx.app.log(TAG, "color " + temp + " " +  "  " + tempI);
		rainfallI = Math.max(rainfallI,  tempI);
		return colorsC[planet.colorIndex][tempI][rainfallI];
		
	}

	public void setAlpha(float alpha){
		this.alpha = Math.max(0, Math.min(1, alpha));
	}
	private float alpha = 0f;

	public boolean sunFromSide = true, planetsFromSide = true;
	int nextRenderPlanet = 0;

	private int renderPlanet;

	private float sunRotation;
	OrthographicCamera camera = new OrthographicCamera();
	public void draw(SpriteBatch screenBatch, ShapeRenderer shape, boolean paused, Sprite pixelSprite, float backgroundRotation){
		//cam.position.rotate(10, 0, 0, 1);
		//if (info == null) return;
		
		for (int i = lerpingOut.size-1; i >= 0; i--){
			if (lerpingOut.get(i) == -1) continue;
			if (lerpingIn.contains(lerpingOut.get(i))){
				lerpingIn.removeValue(lerpingOut.get(i));
			}
			sizeModifier[lerpingOut.get(i)] -= Gdx.graphics.getDeltaTime() * SELECTED_PLANET_ZOOM_SPEED;
			if (sizeModifier[lerpingOut.get(i)] < 1f){
				sizeModifier[lerpingOut.get(i)] = 1f;
				lerpingOut.removeIndex(i);
			}
		}
		for (int i = lerpingIn.size-1; i >= 0; i--){
			
			if (lerpingIn.get(i) == -1) {
				
			}else {
				
				sizeModifier[lerpingIn.get(i)] += Gdx.graphics.getDeltaTime() * SELECTED_PLANET_ZOOM_SPEED;
				if (sizeModifier[lerpingIn.get(i)] > 2f){
					sizeModifier[lerpingIn.get(i)] = 2f;
					lerpingIn.removeIndex(i);
				}
			}
		}
		int currentPlanet = 0;
		boolean hasBufferQueue = true;
		renderPlanet = 0;
		if (info != null){
			currentPlanet = info.currentPlanet;
			Integer rend = renderQueue.poll();
			if (rend != null) {
				renderPlanet = rend;
				SolarSystem currentSystem = info.systems[info.currentSystem];
				Planet planet = currentSystem.planets[renderPlanet];
				planet.makeTexture(surfaceTextureBuffer[renderPlanet], this, screenBatch, pixelSprite);
			} else {
				hasBufferQueue = false;
				if (nextRenderPlanet < info.systems[info.currentSystem].planets.length)
					renderPlanet = nextRenderPlanet++;
				else if (alpha > .95f) {
					renderPlanet = selectedPlanet;
				}
				else
					renderPlanet = currentPlanet;
			}
			//if (renderPlanet != -1)
				//makePlanetTexture(renderPlanet);
		}
		
		
		boolean buffered = false;
		if (info != null 
				){
			if (renderPlanet == -1) {//sun
				
			} else {
				
				mesh.setVertices(verts[renderPlanet]);
				//if (alpha > .925f)buffered =false;
				//Gdx.app.log(TAG, "renderplanet " + renderPlanet);
				//oceanMesh.setVertices(oceanVerts[renderPlanet].toArray());
				//oceanMesh.setIndices(oceanIndices[renderPlanet].toArray());
				toY[renderPlanet] = (renderPlanet + 1) / ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1);
				toX[renderPlanet] = 0.3f;
				SolarSystem currentSystem = info.systems[info.currentSystem];
				Planet planet = currentSystem.planets[renderPlanet];
				if (planet.parent != -1){
					Planet parentPlanet = currentSystem.planets[planet.parent];
					int parentI = parentPlanet.index;
					toY[renderPlanet] = (parentI + 1) / ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1);
					toX[renderPlanet] += .2f + .1f * (planet.parentOrder);
				}

			} 
			//nextRenderPlanet++;
		}
		cam.lookAt(0, 0, 0);
		lightADirection.set(-1f, 1, 1);
		lightBDirection.set(-1f, -1, -1);
		if (!paused && renderPlanet != -1) {
			if (alpha > .95f){
				rotation[renderPlanet] += Gdx.graphics.getDeltaTime() * SELECTED_ROTATION_SPEED;			
			} else
				rotation[renderPlanet] += Gdx.graphics.getDeltaTime() * ROTATION_SPEED;
			lightADirection.rotate(rotation[renderPlanet]*2f * orbitSpeed, 0, 0, 1);
			lightBDirection.rotate(rotation[renderPlanet]*2 * orbitSpeed, 0, 0, 1);
			lightA.set(lightAColor, lightADirection);
			lightB.set(lightBColor, lightBDirection);
			
		}
		//Gdx.gl.glClearColor(.1f, 0, 0, 1f);
		Gdx.gl.glDisable( GL20.GL_CULL_FACE| GL20.GL_BLEND);

    	cam.update();
    	float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
    	float s = 1.1f, ox = -.1f, y = .3f;
    	float toS = .1f;
    	s = MathUtils.lerp(s, toS, alpha);
    	if (renderPlanet != -1) {
    		//planetTextures[renderPlanet].draw(planetPix[renderPlanet], 0, 0);
    		buffer[renderPlanet].begin();
    		//if (renderPlanet != selectedPlanet)
    			
        	modelBatch.begin(cam);
        	//texture.bind();
    		//mesh.render(null, GL20.GL_TRIANGLES);
        	
        	Gdx.gl.glEnable(GL20.GL_DEPTH_TEST );
        	Gdx.gl.glClear( GL20.GL_DEPTH_BUFFER_BIT);
        	Gdx.gl.glClearColor(0f,  0f,  0f,  0f);
        	Gdx.gl.glClear( 
        	    			GL20.GL_COLOR_BUFFER_BIT
        	    			);
        	//planetTexture.bind(0);
    		modelBatch.render(this, environment);
    		modelBatch.end();
    		buffer[renderPlanet].end();
    		if (renderPlanet != selectedPlanet) buffered = true;
    		if (buffered) {
				

				//Gdx.app.log(TAG, "bufferred " + renderPlanet);
    		}
    	}
    	float wh = w/h;
    	//screenBatch.getProjectionMatrix().setToOrtho2D(0f, 0f, wh, 1).translate(-wh*.5f, -.5f, 0).rotate(0,0,1, -backgroundRotation).translate(wh*.5f, .5f, 0f);

    	camera.setToOrtho(false, wh, 1f);
    	camera.rotate(-backgroundRotation);
    	camera.update();
    	screenBatch.setProjectionMatrix(camera.combined);
    	screenBatch.enableBlending();
    	screenBatch.begin();
    	//Gdx.app.log(TAG, "s " + s + "  alpha " + alpha);
    	//if (alpha > .99f)
    	if (info != null){
    		SolarSystem currentSystem = info.systems[info.currentSystem];
    		for (int i = 0; i < MAX_PLANET_VERT_ARRAYS && i < currentSystem.planets.length; i++){
    			if (toY[i] <= 0.0001f) continue;
    			if ( (alpha < .998f && i != renderPlanet ) ) continue;
    			if (buffered) continue;
    			if (hasBufferQueue) continue;
    			Planet planet = currentSystem.planets[i];
    			float x = toX[i];
    			y = MathUtils.lerp(.3f, toY[i], alpha);
    			//if (alpha < .98f) continue;
    			Sprite spr = sprite[i];
    			spr.setSize(s * sizeModifier[i] * planet.size, -s * sizeModifier[i] * planet.size);
    			spr.setCenter(x, y);
    			spr.draw(screenBatch);//draw planet
    		}
    		
    		if (info.systems[0] == null) throw new GdxRuntimeException("null info sys");
    		Sprite sun = Sprites.sun[info.systems[info.currentSystem].sunVariantID];
    		if (selectedPlanet == -1){
    			float alp = 1f;
    			if (alpha < .9f) alp = .1f;
    			sunRotation += Gdx.graphics.getDeltaTime() * SUN_ROTATE_SPEED * alp;
    			//Gdx.app.log(TAG, "rotate sun " + sunRotation);
    		}
    		if (!sunFromSide){
    			sun.setSize(.3f,  .3f);;
    			sun.setCenter(MathUtils.lerp(w/h , w/h * 0.8f, alpha), .5f);
    			sun.setOriginCenter();
    			sun.setRotation(sunRotation);
    			//Gdx.app.log(TAG, "side " + sunRotation);
    		}else{
    			sun.setSize(.3f,  .3f);;
    			sun.setCenter(MathUtils.lerp(w/h , w/h * 1.1f, alpha), .5f);
    			sun.setOriginCenter();
    			sun.setRotation(sunRotation);
    		}
    		sun.draw(screenBatch);;
    	}
    	
    	screenBatch.end();
    	
		
		
		if (alpha < .9f || info == null){
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST | GL20.GL_BLEND | GL20.GL_CULL_FACE);
			return;
		}
		
		shape.setProjectionMatrix(screenBatch.getProjectionMatrix());
		shape.begin(ShapeType.Line);
		float ew = .7f, eh = .2f;;
		if (info.currentOrbitalDepth == GameInfo.ORBIT_ORBIT){
			ew = eh;
		}
		if (currentPlanet >= 0) {
			float ex = toX[currentPlanet] - ew/2, ey = toY[currentPlanet] -eh/2;
			float rot = 0f;
			dv.set(w/h * 0.8f, .5f);
			float planetY = ey;;
			dv.sub(toX[currentPlanet], planetY);
			rot = dv.angle();
			
			dv.set(ew/2f - s/2 - .07f, 0);
			dv.rotate(rot);
			if (toY[currentPlanet] > 0.0001f){
				
				if (info.currentOrbitalDepth == GameInfo.ORBIT_ORBIT){
					shape.ellipse(ex , ey , ew, eh, rot, 24);
				} else 
					shape.ellipse(ex + dv.x, ey + dv.y, ew, eh, rot, 24);
				
			};
		} else {//around sun
			//ew = .8f * 3;
			//eh = .8f * 3;
			//sun.setCenter(w/h * 0.8f, .5f);
			float ex = w/h * 0.8f - ew/2, ey = .5f -eh/2;
			float rot = 0f;
			if (info.currentOrbitalDepth == GameInfo.ORBIT_ORBIT){
				shape.ellipse(ex , ey , ew, eh, rot, 24);
			} else 
				shape.ellipse(ex + dv.x, ey + dv.y, ew, eh, rot, 24);
		}
		
		float width = .1f, height = .1f;
		//shape.rect(toX[selectedPlanet] - width/2, toY[selectedPlanet] - height/2, width, height);
		
		shape.end();
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST | GL20.GL_BLEND | GL20.GL_CULL_FACE);
    }
	
	int nextCachePlanet = 0;

	public void cachePlanet() {
		if (info == null) return;
		if (nextCachePlanet >= info.systems[info.currentSystem].planets.length) return;
		int cachePlanet = nextCachePlanet++;
		//Gdx.app.log(TAG, "cache planet" + cachePlanet);
		sphere.set(baseSphere);
		//sphere.perterb(info.systems[info.currentSystem].planets[cachePlanet].seed);
		Planet planet = info.systems[info.currentSystem].planets[cachePlanet];
		vectorsToVerts(verts[cachePlanet], sphere.vertices, sphere.indices, planet, sphere.textureCoords);
		//resetRenderCached = true;
		renderQueue.put((cachePlanet));
		
	}
	AtomicQueue<Integer> renderQueue = new AtomicQueue<Integer>(30);
    Vector2 dv = new Vector2();
  
	float[] rotation = new float[MAX_PLANET_VERT_ARRAYS];

	private GameInfo info;

	int selectedPlanet;

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		
		Renderable r = pool.obtain();
		r.environment = environment;
		r.material = materials[renderPlanet];
	
		r.meshPart.mesh = mesh;
		r.meshPart.size = faceArray.length;
		r.worldTransform.idt().rotate(0,  0,  1, rotation[renderPlanet]);
		r.meshPart.primitiveType = GL20.GL_TRIANGLES;
		renderables.add(r);
		
		/*r = pool.obtain();
		r.environment = environment;
		r.material = material;
		r.meshPart.mesh = oceanMesh;
		
		r.meshPart.size = oceanMesh.getNumIndices();
		r.worldTransform.idt().rotate(0,  0,  1, rotation[renderPlanet]);
		r.meshPart.primitiveType = GL20.GL_TRIANGLES;
		
		renderables.add(r);*/
		
	}

	public void dispose() {
		mesh.dispose();
		//texture.dispose();
		//pixmap.dispose();
		for (int i = 0; i < buffer.length; i++){
			this.buffer[i].dispose();
			this.buffer[i] = null;
			
		}
		this.modelBatch.dispose();
	}

	public void click(float x, float y, UI ui) {
		y = 1f - y;
		dv.set(x, y);
		float dist = 10000000;
		int index = -2;
		for (int i = 0; i < MAX_PLANET_VERT_ARRAYS; i++){
			//planet = info.systems[info.currentSystem].planets[i];
			float newD = dv.dst2(toX[i], toY[i]);
			if (newD < dist){
				dist = newD;
				index = i;
			}
		}
		
		float highestX = .3f + .2f + .1f * (SolarSystem.MAX_MOONS_PER_PLANET);;
    	float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

		float sunX = w/h * 0.8f;
		float newD = dv.dst2(sunX, 0.5f);
		if (newD < dist){
			dist = newD;
			index = -1;
		}
		if (index != -2 && index != selectedPlanet){
			lerpingOut.add(selectedPlanet);
			selectedPlanet = index;
			lerpingIn.add(selectedPlanet);
		}
		//selectedPlanet = SolarSystem.MAX_PLANETS_PER_SYSTEM-1 - selectedPlanet;
		ui.setPlanetInfo(selectedPlanet);
	}

	public void setInfo(GameInfo info){
		this.info = info;
	}

	public void unSelect() {
		lerpingOut.add(selectedPlanet);
	}
 
	private static class CachePlanetRunnable implements Runnable{
		private PlanetRenderer planet;
		public CachePlanetRunnable(PlanetRenderer planet) {
			this.planet = planet;
		}
		@Override
		public void run() {
			planet.cachePlanet();
			
		}
	}
}