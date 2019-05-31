package ninja.trek;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

import ninja.trek.gen.Planet;

/**
 * Created by n on 18/11/2017.
 */

public class PlanetNode {
    public int index;
    public Array<Connection<PlanetNode>> connections = new Array<Connection<PlanetNode>>();

    public PlanetNode(int i) {
        index = i;
    }

    public enum NodeType {LAND, ORBIT, ELLIPTICAL};
    public NodeType type;
    public Planet planet;
}
