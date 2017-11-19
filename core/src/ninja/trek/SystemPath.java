package ninja.trek;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by n on 19/11/2017.
 */

public class SystemPath implements GraphPath<Connection<PlanetNode>> {
    public Array<SolarSystemGraph.PathConnection> a = new Array<SolarSystemGraph.PathConnection>(true, 16);
    @Override
    public int getCount() {
        return a.size;
    }

    @Override
    public SolarSystemGraph.PathConnection get(int index) {
        return a.get(index);
    }

    @Override
    public void add(Connection<PlanetNode> node) {
        a.add((SolarSystemGraph.PathConnection) node);
    }


    @Override
    public void clear() {
        a.clear();
    }

    @Override
    public void reverse() {
        a.reverse();
    }



    public float cost() {
        float cost = 0;
        for (int i = 0; i < a.size; i++){

            Connection<PlanetNode> c = a.get(i);
            cost += c.getCost();

        }
        return cost;
    }

    @Override
    public Iterator<Connection<PlanetNode>> iterator() {
        return iterator();
    }
}
