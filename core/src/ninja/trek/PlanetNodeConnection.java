package ninja.trek;

import com.badlogic.gdx.ai.pfa.Connection;

/**
 * Created by n on 18/11/2017.
 */

class PlanetNodeConnection implements Connection<PlanetNode> {
    float cost = 1f;
    PlanetNode from, to;
    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public PlanetNode getFromNode() {
        return from;
    }

    @Override
    public PlanetNode getToNode() {
        return to;
    }


}
