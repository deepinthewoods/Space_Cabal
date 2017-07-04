package ninja.trek;
import com.badlogic.gdx.math.MathUtils;
/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ShortArray;

public class Icosphere{ 
private float radius = 1;

private final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
final Array<Vector3> vertices = new Array<Vector3>();

public ShortArray indices = new ShortArray(); // @formatter:on
public ShortArray quads = new ShortArray(); // @formatter:on
public Array<ShortArray> connectedPoints = new Array<ShortArray>(); // @formatter:on

private int horizDivisions;

private int vertDivisions;

Vector3 v = new Vector3();

private int mSegmentsW = 70;

private int mSegmentsH = 70;

private float mRadius = 8f;

private boolean mCreateTextureCoords = false;

private boolean mCreateVertexColorBuffer = false;

private boolean mMirrorTextureCoords;
	
public Icosphere(int horizDivisions, int vertDivisions, float radius) {
	this(horizDivisions, vertDivisions, radius, true);

}
public Icosphere(int horizDivisions, int vertDivisions, float radius, boolean init) {
   this.horizDivisions = horizDivisions;	
   this.vertDivisions = vertDivisions;
   this.radius = radius;
   init(false);
}
public Icosphere(Icosphere other){
	this(other.horizDivisions, other.vertDivisions, other.radius, false);
	set(other);
}

void set(Icosphere ot) {
	for (Vector3 vec : vertices)
		Pools.free(vec);
	vertices.clear();
	for (int i = 0; i < ot.vertices.size; i++){
		Vector3 vec = Pools.obtain(Vector3.class);
		vec.set(ot.vertices.get(i));
		vertices.add(vec);
	}
	indices.clear();
	indices.addAll(ot.indices);
	
}
protected void init(boolean createVBOs) {
	int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
	int numIndices = 2 * mSegmentsW * (mSegmentsH - 1) * 3;

	//float[] vertices = new float[numVertices * 3];
	//float[] normals = new float[numVertices * 3];
	short[] indices = new short[numIndices];

	int i, j;
	int vertIndex = 0, index = 0;
	final float normLen = 1.0f / mRadius ;

	for (j = 0; j <= mSegmentsH; ++j) {
		float horAngle = (float) (Math.PI * j / mSegmentsH);
		float z = mRadius * (float) Math.cos(horAngle);
		float ringRadius = mRadius * (float) Math.sin(horAngle);

		for (i = 0; i <= mSegmentsW; ++i) {
			float verAngle = (float) (2.0f * Math.PI * i / mSegmentsW);
			float x = ringRadius * (float) Math.cos(verAngle);
			float y = ringRadius * (float) Math.sin(verAngle);

//			normals[vertIndex] = x * normLen;
//			vertices[vertIndex++] = x;
//			normals[vertIndex] = z * normLen;
//			vertices[vertIndex++] = z;
//			normals[vertIndex] = y * normLen;
//			vertices[vertIndex++] = y;
			
			this.vertices.add(new Vector3(x, y, z));
			vertIndex+=3;
			if (j == mSegmentsH && i > 0 && j > 0 && j != mSegmentsH && j != 1){
				short a = (short) ((mSegmentsW + 1) * j + i - mSegmentsH);
				short b = (short) ((mSegmentsW + 1) * j + i - 1 - mSegmentsH);
				short c = (short) ((mSegmentsW + 1) * (j - 1) + i - 1);
				short d = (short) ((mSegmentsW + 1) * (j - 1) + i);
				
				indices[index++] = a;
				indices[index++] = b;
				indices[index++] = c;
				indices[index++] = a;
				indices[index++] = c;
				indices[index++] = d;
				quads.add(a);
				quads.add(b);
				quads.add(c);
				quads.add(d);
			} else 
			if (i > 0 && j > 0) {
				short a = (short) ((mSegmentsW + 1) * j + i);
				short b = (short) ((mSegmentsW + 1) * j + i - 1);
				short c = (short) ((mSegmentsW + 1) * (j - 1) + i - 1);
				short d = (short) ((mSegmentsW + 1) * (j - 1) + i);

				if (j == mSegmentsH) {
					indices[index++] = a;
					indices[index++] = c;
					indices[index++] = d;
				} else if (j == 1) {
					indices[index++] = a;
					indices[index++] = b;
					indices[index++] = c;
				} else {
					indices[index++] = a;
					indices[index++] = b;
					indices[index++] = c;
					indices[index++] = a;
					indices[index++] = c;
					indices[index++] = d;
					quads.add(a);
					quads.add(b);
					quads.add(c);
					quads.add(d);
				}
			}
		}
		
		/*for (int verti = 0; verti < vertices.size; verti++){
			ShortArray list = new ShortArray(); 
			connectedPoints.add(list);;
			list.add(verti);
			for (int indk = 0; indk < indices.length/3-2; indk++){
				
				if (indices[indk*3] == verti){
					short value;
					value = indices[indk*3+1];
					if (!list.contains(value))list.add(value);
					value = indices[indk*3+2];
					if (!list.contains(value))list.add(value);
				} else if (indices[indk*3+1] == verti){
					short value;
					value = indices[indk*3];
					if (!list.contains(value))list.add(value);
					value = indices[indk*3+2];
					if (!list.contains(value))list.add(value);
				}
				else if (indices[indk*3+2] == verti){
					short value;
					value = indices[indk*3];
					if (!list.contains(value))list.add(value);
					value = indices[indk*3+1];
					if (!list.contains(value))list.add(value);
				}
				
			}
		}*/
		
		
	}

	float[] textureCoords = null;
	if (mCreateTextureCoords) {
		int numUvs = (mSegmentsH + 1) * (mSegmentsW + 1) * 2;
		textureCoords = new float[numUvs];

		numUvs = 0;
		for (j = 0; j <= mSegmentsH; ++j) {
			for (i = mSegmentsW; i >= 0; --i) {
                float u = (float) i / mSegmentsW;
				textureCoords[numUvs++] = mMirrorTextureCoords ? 1.0f - u : u;
				textureCoords[numUvs++] = (float) j / mSegmentsH;
			}
		}
	}

	float[] colors = null;

	if (mCreateVertexColorBuffer)
	{
		int numColors = numVertices * 4;
		colors = new float[numColors];
		for (j = 0; j < numColors; j += 4)
		{
			colors[j] = 1.0f;
			colors[j + 1] = 0;
			colors[j + 2] = 0;
			colors[j + 3] = 1.0f;
		}
	}

	//setData(vertices, normals, textureCoords, colors, indices, createVBOs);
	this.vertices.addAll(vertices);
	this.indices.addAll(indices);;
}
	public void perterb(int seed) {
		MathUtils.random.setSeed(seed);
		if (true) return;
		for (int i = 0; i < 100000; i++){
			//if (true) continue;
			int quadIndex = MathUtils.random(quads.size / 4 - 4);
			;
			
			Vector3 a = vertices.get(quads.get(quadIndex*4));
			Vector3 b = vertices.get(quads.get(quadIndex*4+1));
			Vector3 c = vertices.get(quads.get(quadIndex*4+2));
			Vector3 d = vertices.get(quads.get(quadIndex*4+3));
			v.set(a).add(b).add(c).add(d).scl(.25f);
			float alpha = MathUtils.random(.5f);
			a.lerp(v, alpha).nor();
			b.lerp(v, alpha).nor();
			c.lerp(v, alpha).nor();
			d.lerp(v, alpha).nor();
		}
	}

/*
 * for (int i = 0; i < 1000000; i++){
			ShortArray points = connectedPoints.get(MathUtils.random(connectedPoints.size));
			v.set(0,0,0);
			for (int k = 0; k < points.size; k++){
				v.add(vertices.get(points.get(k)));
			}
			v.scl(1f/points.size);
			
		}
 */
}