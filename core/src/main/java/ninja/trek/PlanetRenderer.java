package ninja.trek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.UI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;


 
/**
 * Approximates a sphere as a simplicial polyhedron, formed by subdividing the triangles of a regular icosahedron into
 * smaller triangles. Normally used to achieve a more isotropical layout of vertices than a UV sphere.
 *
 * @author jayfella
 */
 
// to see inside, change indices from 1-2-3 to 1-3-2.
public class PlanetRenderer implements RenderableProvider{
 
    private static final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
 
    private static final Vector3[] ico_vertices = {
            new Vector3(-1,  t,  0), new Vector3( 1,  t,  0), new Vector3(-1, -t,  0), new Vector3( 1, -t,  0),
            new Vector3( 0, -1,  t), new Vector3( 0,  1,  t), new Vector3( 0, -1, -t), new Vector3( 0,  1, -t),
            new Vector3( t,  0, -1), new Vector3( t,  0,  1), new Vector3(-t,  0, -1), new Vector3(-t,  0,  1)
    };
 
    private static final short[] ico_indices = {
            0, 11, 5,   0, 5, 1,    0, 1, 7,    0, 7, 10,   0, 10, 11,
            1, 5, 9,    5, 11, 4,   11, 10, 2,  10, 7, 6,   7, 1, 8,
            3, 9, 4,    3, 4, 2,    3, 2, 6,    3, 6, 8,    3, 8, 9,
            4, 9, 5,    2, 4, 11,   6, 2, 10,   8, 6, 7,    9, 8, 1
    };

	private static final int VERTEX_TOTAL_FLOATS = 7;

	private static final String TAG = "planet";

	private static final float ROTATION_SPEED = 8;

	private static final int BUFFER_SIZE = 200;

	private static final int MAX_PLANET_VERT_ARRAYS = 12;
 
    private Map<Long, Integer> middlePointIndexCache = new HashMap<>();
    
    private List<Vector3> vertices = new ArrayList<>();

	private Environment environment;

	private ModelBatch modelBatch;

	private PerspectiveCamera cam;

	private Mesh mesh;

	private float[][] verts;

	private Texture texture;

	private Material material;

	private short[] faceArray;

	private Icosphere sphere;

	private Pixmap pixmap;
	
	private Color[][] colors;
	
	private Vector3 lightADirection = new Vector3(), lightBDirection = new Vector3();

	private DirectionalLight lightA;

	private DirectionalLight lightB;
	
	private Color lightAColor = new Color(Color.WHITE), lightBColor = new Color(.1f, .1f, .2f, 1f);
	
	float seed = MathUtils.random();
    
	Vector3 v = new Vector3();
	
    SimplexNoise noise = new SimplexNoise();

	private float orbitSpeed = 1f;

	private FrameBuffer[] buffer = new FrameBuffer[SolarSystem.MAX_PLANETS_PER_SYSTEM];
	
	private Sprite[] sprite = new Sprite[SolarSystem.MAX_PLANETS_PER_SYSTEM];

	private Vector3[] SpherePointArray;
	
 
    public PlanetRenderer(int recursionLevel, float size) {
    	texture = new Texture(Gdx.files.internal("biomes.png"));
    	pixmap = new Pixmap(Gdx.files.internal("biomes.png"));
    	
    	colors = new Color[pixmap.getWidth()][pixmap.getHeight()];
    	for (int i = 0; i < pixmap.getWidth(); i++){
    		for (int k = 0; k < pixmap.getHeight(); k++){
    			int p = pixmap.getPixel(i, k);
    			Color c = new Color(p);
    			
    			colors[i][k] = c;
    		}
    	}
    	colors[0][0].set(pixmap.getPixel(pixmap.getWidth()-1,  pixmap.getHeight()-1));
    	//colors[0][0].set(pixmap.getPixel(0, 0));
    	Gdx.app.log("planet", "pixel " + colors[0][0].r + " " + colors[0][0].g + " " + colors[0][0].b + " " + pixmap.getWidth());
    	texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    	sphere = new Icosphere(5, 8f);
 
        environment = new Environment();
		lightA = new DirectionalLight();
		lightA.set(Color.WHITE, lightADirection);
		
		environment.add(lightA);
		lightB = new DirectionalLight();
		lightB.set(Color.DARK_GRAY, lightBDirection);
		environment.add(lightB);
        	
		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(50, BUFFER_SIZE, BUFFER_SIZE);
		cam.position.set(0f, 20f, 0f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		//Gdx.app.log("sphere", "type " + sphere.vertices.toArray().getClass());
		
		SpherePointArray = sphere.vertices.toArray();
		faceArray = new short[sphere.indices.size];
		mesh = new Mesh(true, SpherePointArray.length * VERTEX_TOTAL_FLOATS, faceArray.length, 
				VertexAttribute.Position()
				, VertexAttribute.ColorPacked()
				//, VertexAttribute.TexCoords(0)
				, VertexAttribute.Normal()
				);
		material = new Material(
				);
		//material.set(TextureAttribute.createDiffuse(texture));
		//material.set(TextureAttribute.createAmbient(texture));
		//material.set(new ColorAttribute(ColorAttribute.createAmbient(Color.WHITE)));
		//material.set(new ColorAttribute(ColorAttribute.AmbientLight));
		
        verts = new float[MAX_PLANET_VERT_ARRAYS][];
        verts[0] = new float[SpherePointArray.length * VERTEX_TOTAL_FLOATS];
        vectorsToVerts(verts[0], SpherePointArray);
        
        mesh.setVertices(verts[0]);
        
        faceArray =  sphere.indices.toArray();
        
        mesh.setIndices(faceArray);
        
        /*FloatBuffer pb = BufferUtils.createFloatBuffer(vertexArray);
        setBuffer(VertexBuffer.Type.Position, 3, pb);
 
        IntBuffer ib = BufferUtils.createIntBuffer(faceArray);
        setBuffer(VertexBuffer.Type.Index, 3, ib);
 
        FloatBuffer tb = BufferUtils.createFloatBuffer(texArray);
        setBuffer(VertexBuffer.Type.TexCoord, 2, tb);
 
        updateBound();*/
        for (int i = 0; i < buffer.length; i++){
        	buffer[i] = new FrameBuffer(Format.RGBA4444, BUFFER_SIZE, BUFFER_SIZE, false);
        	buffer[i].getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        	sprite[i] = new Sprite(buffer[i].getColorBufferTexture());
        }
        
    }
    
    private void vectorsToVerts(float[] verts, Vector3[] vertexArray) {
    	int p = 0, i = 0, k = 0;
    	float lowestR = 2f, highestR = 0f;
    	
    	while ( p < verts.length){
    		//Gdx.app.log(TAG, "did " + vertexArray[i]);
    		Vector3 pos = vertexArray[i];
    		float height = noise.scaled(v.x,  v.y,  v.z, seed, 2f) ;
    		height *= .25f;
    		
    		pos.nor().scl(8);
    		//pos.scl(1 + height);
    		v.set(pos);
    		v.nor();
    		
    		verts[p++] = pos.y;
    		verts[p++] = pos.x;
    		verts[p++] = pos.z;
    		
    		float temp = noise.scaled(v.x,  v.y,  v.z, seed, 1f) /2f+.5f;
    		temp *= .45f;
    		temp =  (1f-Math.abs(v.z));
    		//temp = 1f;
    		float rainfall = noise.scaled(v.x,  v.y,  v.z, seed, 1f) /2f+.5f;
    		rainfall *= 2f;
    		rainfall -= .6f;
    		
    		lowestR = Math.min(lowestR,  rainfall);
    		highestR = Math.max(highestR,  rainfall);
    		//if (rainfall < .2f)
    			//Gdx.app.log(TAG, "low rainfall" + rainfall + " " + lowestR + "  high " + highestR);
    		rainfall = Math.max(Math.min(rainfall, 1f), 0f);
    		
    		//temp = MathUtils.random(1f);
    		rainfall *= temp;
    		
    		verts[p++] = lookupColor(1f-temp, 1f-rainfall, height);
    		//verts[p++] = 1f - temp;
    		//verts[p++] = 1f - rainfall;

    		
    		//v.set(0, 0, 0);
    		
    		
    				
    		verts[p++] = v.x;    	
    		verts[p++] = v.y;    	
    		verts[p++] = v.z;   
    		//if (!done) Gdx.app.log(TAG, "jskl " +  i);
    		///WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    		//SDKFAAAAAAAAAAAAAAAAAAAAAAAA
    		i++;
    	}
    
    }


	


	private float lookupColor(float temp, float rainfall, float height) {
		
		int tempI = (int) ((temp) * (colors.length-1));
		int rainfallI = (int) ((rainfall) * (colors[0].length-1));
		//tempI = colors.length-3;
		//rainfallI = colors.length-3;
		//tempI = 63;
		//rainfallI = 63;
		//tempI = 0;
		//rainfallI = 0;
		//return colors[0][0].toFloatBits();
		return colors[tempI][rainfallI].toFloatBits();
		
	}


	public void setAlpha(float alpha){
		this.alpha = alpha;
	}
	private float alpha = 0f;

	public boolean starsFromSide = true, planetsFromSide = true;

	public void draw(SpriteBatch screenBatch, ShapeRenderer shape){
		//cam.position.rotate(10, 0, 0, 1);
		//if (info == null) return;
		
		int currentPlanet = 1;
		int renderPlanet = 0;
		if (info != null){
			currentPlanet = info.currentPlanet;
			if (alpha > .99f){
				renderPlanet = MathUtils.random(info.systems[info.currentSystem].planets.length-1);
			} else renderPlanet = currentPlanet;
		}
		
		
		
		if (buffer[renderPlanet] == null){
			buffer[renderPlanet] = new FrameBuffer(Format.RGBA4444, BUFFER_SIZE, BUFFER_SIZE, false);
			sprite[renderPlanet] = new Sprite(buffer[currentPlanet].getColorBufferTexture());
		}
		
		if (alpha > .99f){
			if (verts[renderPlanet] == null){
				verts[renderPlanet] = new float[SpherePointArray.length * VERTEX_TOTAL_FLOATS];
			}
			vectorsToVerts(verts[renderPlanet], SpherePointArray);
			mesh.setVertices(verts[renderPlanet]);
		}
		cam.lookAt(0, 0, 0);
		lightADirection.set(.5f, 1, 1);
		lightBDirection.set(1, -1, -1);
		rotation += Gdx.graphics.getDeltaTime() * ROTATION_SPEED;
		lightADirection.rotate(rotation*2f * orbitSpeed, 0, 0, 1);
		lightBDirection.rotate(rotation*2 * orbitSpeed, 0, 0, 1);
		lightA.set(lightAColor, lightADirection);
		lightB.set(lightBColor, lightBDirection);
		//Gdx.gl.glClearColor(.1f, 0, 0, 1f);
    	Gdx.gl.glClear( GL20.GL_DEPTH_BUFFER_BIT
    	//		| GL20.GL_COLOR_BUFFER_BIT
    			);
    	//Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    	cam.update();
    	buffer[renderPlanet].begin();
    	modelBatch.begin(cam);
    	texture.bind();
		//mesh.render(null, GL20.GL_TRIANGLES);
		
		modelBatch.render(this, environment);
		modelBatch.end();
		buffer[renderPlanet].end();
		
		float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
		screenBatch.getProjectionMatrix().setToOrtho2D(0, 0, w/h, 1);
		screenBatch.enableBlending();
		screenBatch.begin();
		float s = .8f, ox = -.1f, y = .3f;
		float toS = .1f;
		s = MathUtils.lerp(s, toS, alpha);
		//if (alpha > .99f)
		for (int i = 0; i < SolarSystem.MAX_PLANETS_PER_SYSTEM; i++){
			//w = 1;h = 1;
			float toY = (i + 1) / ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1);
			y = MathUtils.lerp(.3f, toY, alpha);
			Sprite spr = sprite[i];
			
			spr.setSize(s, -s);
			spr.setCenter(.3f, y);
			
			spr.draw(screenBatch);;
			
		}
		if (info != null){
			
			if (info.systems[0] == null) throw new GdxRuntimeException("null info sys");
			Sprite sun = Sprites.sun[info.systems[info.currentSystem].sunVariantID];
			if (starsFromSide){
				sun.setSize(.3f,  .3f);;
				sun.setCenter(MathUtils.lerp(w/h , w/h * 0.8f, alpha), .5f);
			}else{
				sun.setSize(s* 3, -s * 3);
				sun.setSize(.8f * 3,  .8f * 3);;
				sun.setCenter(w/h * 0.8f, .5f);
			}
			sun.draw(screenBatch);;
		}
		
		
		screenBatch.end();
		
		if (alpha < .99f) return;
		shape.setProjectionMatrix(screenBatch.getProjectionMatrix());
		shape.begin(ShapeType.Line);
		float ew = .7f, eh = .2f;;
		if (info.currentOrbitalDepth == GameInfo.ORBIT_ORBIT){
			ew = eh;
		}
		float ex = .3f - ew/2, ey = (currentPlanet + 1) / ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1) - eh/2;
		float rot = 0f;
		dv.set(w/h * 0.8f, .5f);
		float planetY = (currentPlanet + 1) / ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1);;
		dv.sub(.3f, planetY);
		rot = dv.angle();
		
		
		
		dv.set(ew/2f - s/2 - .07f, 0);
		dv.rotate(rot);
		if (info.currentOrbitalDepth == GameInfo.ORBIT_ORBIT){

			shape.ellipse(ex , ey , ew, eh, rot, 24);
		} else 
			shape.ellipse(ex + dv.x, ey + dv.y, ew, eh, rot, 24);
		
		
		shape.end();
		
		//Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
    }
    Vector2 dv = new Vector2();
    
    
    
  
	float rotation = 0;

	private GameInfo info;

	private int selectedPlanet;

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		
		Renderable r = pool.obtain();
		r.environment = environment;
		
		r.material = material;
		r.meshPart.mesh = mesh;
		r.meshPart.size = faceArray.length;
		r.worldTransform.idt().rotate(0,  0,  1, rotation);
		r.meshPart.primitiveType = GL20.GL_TRIANGLES;
		
		renderables.add(r);
		
		
	}

	public void dispose() {
		mesh.dispose();
		texture.dispose();
		pixmap.dispose();
		for (int i = 0; i < buffer.length; i++){
			this.buffer[i].dispose();
			this.buffer[i] = null;
			
		}
		this.modelBatch.dispose();
	}

	public void click(float x, float y, UI ui) {
		if (x > .6f) selectedPlanet = -1;
		float planetY = ((float)SolarSystem.MAX_PLANETS_PER_SYSTEM+1);
		selectedPlanet = (int) (
				(y + 0)
				* (SolarSystem.MAX_PLANETS_PER_SYSTEM+1) * 2
				);
		selectedPlanet--;
		selectedPlanet /= 2;
		selectedPlanet = Math.min(Math.max(selectedPlanet,  0), SolarSystem.MAX_PLANETS_PER_SYSTEM-1);
		ui.setPlanetInfo(selectedPlanet);
	}
	
	public void setInfo(GameInfo info){
		this.info = info;
	}
 
}