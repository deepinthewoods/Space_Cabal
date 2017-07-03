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
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
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

	private static final float SELECTED_ROTATION_SPEED = 140;
	
	private static final int BUFFER_SIZE = 300;

	private static final int MAX_PLANET_VERT_ARRAYS = SolarSystem.MAX_PLANETS_PER_SYSTEM + SolarSystem.MAX_OTHER_BODIES_PER_SYSTEM;

	private static final float SELECTED_PLANET_ZOOM_SPEED = 3f;
 
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

	private FrameBuffer[] buffer = new FrameBuffer[MAX_PLANET_VERT_ARRAYS];
	
	private Sprite[] sprite = new Sprite[MAX_PLANET_VERT_ARRAYS];
	
	private float[] toX = new float[MAX_PLANET_VERT_ARRAYS], toY = new float[MAX_PLANET_VERT_ARRAYS];

	private float[] sizeModifier = new float[MAX_PLANET_VERT_ARRAYS];

	private Vector3[] SpherePointArray;

	private FloatArray[] oceanVerts;

	private ShortArray[] oceanIndices;

	private Mesh oceanMesh;
	
	private IntArray lerpingIn = new IntArray(), lerpingOut = new IntArray();
 
    public PlanetRenderer(int recursionLevel, float size) {
    	for (int i = 0; i < sizeModifier.length; i++)
    		sizeModifier[i] = 1f;
    	
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
    	//Gdx.app.log("planet", "pixel " + colors[0][0].r + " " + colors[0][0].g + " " + colors[0][0].b + " " + pixmap.getWidth());
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
		oceanMesh = new Mesh(true, SpherePointArray.length * VERTEX_TOTAL_FLOATS, faceArray.length * 2, 
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
        oceanVerts = new FloatArray[MAX_PLANET_VERT_ARRAYS];
        oceanIndices = new ShortArray[MAX_PLANET_VERT_ARRAYS];
        oceanVerts[0] = new FloatArray();
        oceanIndices[0] = new ShortArray();
        verts[0] = new float[SpherePointArray.length * VERTEX_TOTAL_FLOATS];
        vectorsToVerts(verts[0], SpherePointArray, oceanVerts[0], oceanIndices[0], sphere.indices);
        
        mesh.setVertices(verts[0]);
        
        faceArray =  sphere.indices.toArray();
        
        mesh.setIndices(faceArray);
        
        oceanMesh.setVertices(oceanVerts[0].toArray());
		oceanMesh.setIndices(oceanIndices[0].toArray());
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
    
    private void vectorsToVerts(float[] verts, Vector3[] vectorArray, FloatArray oceanVerts, ShortArray oceanIndices, ShortArray indices) {
    	int p = 0, i = 0, k = 0;
    	float lowestR = 2f, highestR = 0f;
    	
    	while ( p < verts.length){
    		//Gdx.app.log(TAG, "did " + vertexArray[i]);
    		Vector3 pos = vectorArray[i];
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
    	int indexIndex = 0;
    	for (i = 0; i < indices.size/3-3; i++){
    		v.set(0, 0, 0);
    		//Vector3 vertA = vectorArray[indices.get(i*3)];
    		//Vector3 vertB = vectorArray[indices.get(i*3+1)];
    		//Vector3 vertC = vectorArray[indices.get(i*3+2)];
    		int vi;
    		
    		
    		vi = indices.get(i*3) * VERTEX_TOTAL_FLOATS;
    		v.set(verts[vi], verts[vi+1], verts[vi+2]);
    		float oceanA = noise.scaled(v.x, v.y, v.z, seed, .2f);
    		
    		vi = indices.get(i*3+1) * VERTEX_TOTAL_FLOATS;
    		v.set(verts[vi], verts[vi+1], verts[vi+2]);
    		float oceanB = noise.scaled(v.x, v.y, v.z, seed, .2f);
    		
    		vi = indices.get(i*3+2) * VERTEX_TOTAL_FLOATS;
    		v.set(verts[vi], verts[vi+1], verts[vi+2]);
    		float oceanC = noise.scaled(v.x, v.y, v.z, seed, .2f);
    		
    		v.scl(1f/3f);
    		
    		//float ocean = noise.scaled(v.x, v.y, v.z, seed, .2f);
    		
    		
    		
    		if (oceanA > 0 || oceanB > 0 || oceanC > 0){
    			float colorA = Color.BLUE.toFloatBits();
    			float colorB = Color.BLUE.toFloatBits();
    			float colorC = Color.BLUE.toFloatBits();
    			if (oceanA < .031f){
    				colorA = Color.YELLOW.toFloatBits();
    			
    			}
    			if (oceanB < .031f){
    				colorB = Color.YELLOW.toFloatBits();
    			}
    			if (oceanC < .031f){
    				colorC = Color.YELLOW.toFloatBits();
    			}
    				
    			vi = indices.get(i*3) * VERTEX_TOTAL_FLOATS;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(colorA);vi++;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		vi = indices.get(i*3+1) * VERTEX_TOTAL_FLOATS;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(colorB);vi++;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		vi = indices.get(i*3+2) * VERTEX_TOTAL_FLOATS;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(colorC);vi++;
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		oceanVerts.add(verts[vi++]);
        		
        		oceanIndices.add(indexIndex++);
        		oceanIndices.add(indexIndex++);
        		oceanIndices.add(indexIndex++);
    		}
    		
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
		this.alpha = Math.max(0, Math.min(1, alpha));
	}
	private float alpha = 0f;

	public boolean starsFromSide = true, planetsFromSide = true;
	int nextRenderPlanet = 0;

	private int renderPlanet;
	public void draw(SpriteBatch screenBatch, ShapeRenderer shape){
		//cam.position.rotate(10, 0, 0, 1);
		//if (info == null) return;
		for (int i = lerpingIn.size-1; i >= 0; i--){
			if (lerpingOut.contains(lerpingIn.get(i))){
				lerpingOut.removeValue(lerpingIn.get(i));
			}
			sizeModifier[lerpingIn.get(i)] += Gdx.graphics.getDeltaTime() * SELECTED_PLANET_ZOOM_SPEED;
			if (sizeModifier[lerpingIn.get(i)] > 2f){
				sizeModifier[lerpingIn.get(i)] = 2f;
				lerpingIn.removeIndex(i);
			}
		}
		for (int i = lerpingOut.size-1; i >= 0; i--){
			sizeModifier[lerpingOut.get(i)] -= Gdx.graphics.getDeltaTime() * SELECTED_PLANET_ZOOM_SPEED;
			if (sizeModifier[lerpingOut.get(i)] < 1f){
				sizeModifier[lerpingOut.get(i)] = 1f;
				lerpingOut.removeIndex(i);
			}
		}
		int currentPlanet = 0;
		renderPlanet = 0;
		if (info != null){
			currentPlanet = info.currentPlanet;
			if (alpha > .25f){
				renderPlanet = nextRenderPlanet++;
				if (nextRenderPlanet >= info.systems[info.currentSystem].planets.length) renderPlanet = selectedPlanet;
			} else renderPlanet = currentPlanet;
		}
		
		if (buffer[renderPlanet] == null){
			buffer[renderPlanet] = new FrameBuffer(Format.RGBA4444, BUFFER_SIZE, BUFFER_SIZE, false);
			sprite[renderPlanet] = new Sprite(buffer[currentPlanet].getColorBufferTexture());
		}
		
		if (verts[renderPlanet] == null){
			verts[renderPlanet] = new float[SpherePointArray.length * VERTEX_TOTAL_FLOATS];
			oceanVerts[renderPlanet] = new FloatArray();
			oceanIndices[renderPlanet] = new ShortArray();
		}
		
		if (alpha > .25f && nextRenderPlanet < info.systems[info.currentSystem].planets.length){
			vectorsToVerts(verts[renderPlanet], SpherePointArray, oceanVerts[renderPlanet], oceanIndices[renderPlanet], sphere.indices);
			mesh.setVertices(verts[renderPlanet]);
			oceanMesh.setVertices(oceanVerts[renderPlanet].toArray());
			oceanMesh.setIndices(oceanIndices[renderPlanet].toArray());
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
		cam.lookAt(0, 0, 0);
		lightADirection.set(.5f, 1, 1);
		lightBDirection.set(1, -1, -1);
		if (alpha > .95f){
			rotation[renderPlanet] += Gdx.graphics.getDeltaTime() * SELECTED_ROTATION_SPEED;			
		} else
			rotation[renderPlanet] += Gdx.graphics.getDeltaTime() * ROTATION_SPEED;
		lightADirection.rotate(rotation[renderPlanet]*2f * orbitSpeed, 0, 0, 1);
		lightBDirection.rotate(rotation[renderPlanet]*2 * orbitSpeed, 0, 0, 1);
		lightA.set(lightAColor, lightADirection);
		lightB.set(lightBColor, lightBDirection);
		//Gdx.gl.glClearColor(.1f, 0, 0, 1f);
    	Gdx.gl.glClear( GL20.GL_DEPTH_BUFFER_BIT
    	//		| GL20.GL_COLOR_BUFFER_BIT
    			);
    	Gdx.gl.glEnable(GL20.GL_CULL_FACE);
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
		float toS = .08f;
		s = MathUtils.lerp(s, toS, alpha);
		//Gdx.app.log(TAG, "s " + s + "  alpha " + alpha);
		//if (alpha > .99f)
		if (info != null){
			SolarSystem currentSystem = info.systems[info.currentSystem];
			for (int i = 0; i < MAX_PLANET_VERT_ARRAYS && i < currentSystem.planets.length; i++){
				if (toY[i] <= 0.0001f) continue;
				if (alpha < .98f && i != renderPlanet) continue;
				//Planet planet = currentSystem.planets[i];
				float x = toX[i];
				y = MathUtils.lerp(.3f, toY[i], alpha);
				Sprite spr = sprite[i];
				
				spr.setSize(-s * sizeModifier[i], -s * sizeModifier[i]);
				spr.setCenter(x, y);
				spr.draw(screenBatch);;
			}
			
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
		float width = .1f, height = .1f;
		//shape.rect(toX[selectedPlanet] - width/2, toY[selectedPlanet] - height/2, width, height);
		
		shape.end();
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
    }
    Vector2 dv = new Vector2();
  
	float[] rotation = new float[MAX_PLANET_VERT_ARRAYS];

	private GameInfo info;

	int selectedPlanet;

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		
		Renderable r = pool.obtain();
		r.environment = environment;
		r.material = material;
		r.meshPart.mesh = mesh;
		r.meshPart.size = faceArray.length;
		r.worldTransform.idt().rotate(0,  0,  1, rotation[renderPlanet]);
		r.meshPart.primitiveType = GL20.GL_TRIANGLES;
		
		renderables.add(r);
		
		r = pool.obtain();
		r.environment = environment;
		r.material = material;
		r.meshPart.mesh = oceanMesh;
		
		r.meshPart.size = oceanMesh.getNumIndices();
		r.worldTransform.idt().rotate(0,  0,  1, rotation[renderPlanet]);
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
		y = 1f - y;
		dv.set(x, y);
		float dist = 10000000;
		int index = -1;
		for (int i = 0; i < MAX_PLANET_VERT_ARRAYS; i++){
			//planet = info.systems[info.currentSystem].planets[i];
			float newD = dv.dst2(toX[i], toY[i]);
			if (newD < dist){
				dist = newD;
				index = i;
			}
		}
		if (index != -1 && index != selectedPlanet){
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
 
}