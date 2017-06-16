package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

public class Icosphere{ 
private int lod = 0;
private float radius = 1;

private final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
final Array<Vector3> vertices = new Array<Vector3>(new Vector3[] {// @formatter:off
                new Vector3(-1f, t, 0),
                new Vector3(+1f, t, 0),
                new Vector3(-1f, -t, 0),
                new Vector3(+1f, -t, 0),
                new Vector3(0f, -1, t),
                new Vector3(0f, 1, t),
                new Vector3(0f, -1, -t),
                new Vector3(0f, 1, -t),
                new Vector3(t, 0, -1),
                new Vector3(t, 0, 1),
                new Vector3(-t, 0, -1),
                new Vector3(-t, 0, 1), });

public ShortArray indices = new ShortArray(new short[]{
          0, 11,
         
          5,  

          0,  5,
         
          1,  

          0,  1,
          
          7,  

          0,  7,
          
         10, 

          0, 10,
        
         11,  

          1,  5,
          
          9,  

          5, 11,
         
          4,  

         11, 10,
        
          2, 

         10,  7,
        
         6, 

         7,  1,
         
         8,  

         3,  9,
         
         4,  

         3,  4,
         
         2,  

         3,  2,
         
         6,  

         3,  6,
         
         8,  

         3,  8,
        
         9,  

         4,  9,
       
         5,  

         2,  4,
        
        11,  

         6,  2,
        
        10,  

         8,  6,
        
         7,  

         9,  8,
         
         1,  
}); // @formatter:on
	
public Icosphere(int lod, float radius) {
    this.lod = lod;
    this.radius = radius;
    splitPlanes(lod);
    normalize();
}

/*@Override
protected IsRenderable setupMesh() {
    splitPlanes(lod);
    final WireframeMeshBuilder builder = new WireframeMeshBuilder();
    builder.setVertices(vertices);
    builder.setIndices(indices);
    builder.setLinePrimitive(GL11.GL_LINES);
    builder.setColor(color);
    builder.setInitRotation(initialRotation);
    builder.setInitTranslation(initialTranslation);
    return builder.build();
}*/

private void normalize() {
    for (final Vector3 vec : vertices) {
        final float l = vec.len();
        vec.scl(radius / l);
    }
}

private void splitPlanes(final int iterations) {
    for (int i = 0; i < iterations; i++) {
        splitPlanes();
    }
}

void splitPlanes() {
	//Gdx.app.log("ico", "split plajnes");
    // for each side we have 4 new smaller sides now
    final short[] indices2 = new short[indices.size * 4];
    final int indicesPerSide = 3;
    final int newIndices = indicesPerSide * 4;

    for (int i = 0; i < indices.size; i += indicesPerSide) {

        // get the start of the lines of a side
        final Vector3 v1 = vertices.get(indices.get(i + 0)); // top
        final Vector3 v2 = vertices.get(indices.get(i + 1)); // left
        final Vector3 v3 = vertices.get(indices.get(i + 2)); // right

        // find the midpoints
        final Vector3 n1 = splitLine(v1, v2); // mid-left
        final Vector3 n2 = splitLine(v2, v3); // mid-bottom
        final Vector3 n3 = splitLine(v3, v1); // mid-right

        // to keep them on the sphere
        n1.scl(radius / n1.len());
        n2.scl(radius / n2.len());
        n3.scl(radius / n3.len());

        final short offset = (short) vertices.size;
        vertices.add(n1); // offset + 0
        vertices.add(n2); // offset + 1
        vertices.add(n3); // offset + 2

        // top triangle
        final int j = i / indicesPerSide * newIndices;
        indices2[j + 0] = indices.get(i + 0); // top
        indices2[j + 1] = (short) (offset + 0);
        
        indices2[j + 2] = (short) (offset + 2); // mid-right

        // left triangle
        indices2[j + 3] = (short) (offset + 0); // mid-left
        indices2[j + 4] = indices.get(i + 1);
        indices2[j + 5] = (short) (offset + 1); // mid-bottom

        // right triangle
        indices2[j + 6] = (short) (offset + 2); // mid-right
        indices2[j + 7] = (short) (offset + 1);
        indices2[j + 8] = indices.get(i + 2); // right

        // center triangle
        indices2[j + 9] = (short) (offset + 0); // mid-left
        indices2[j + 10] = (short) (offset + 1);
        indices2[j + 11] = (short) (offset + 2); // mid-right
   }
    indices.clear();
    indices.addAll(indices2);
}

private Vector3 splitLine(final Vector3 v1, final Vector3 v2) {
    return new Vector3((v1.x + v2.x) / 2f, (v1.y + v2.y) / 2f, (v1.z + v2.z) / 2f);
}




}