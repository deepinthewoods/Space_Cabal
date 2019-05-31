package ninja.trek;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

import ninja.trek.gen.GameInfo;
import ninja.trek.gen.SolarSystem;

/**
 * Created by n on 18/11/2017.
 */

public class SolarSystemGraph implements IndexedGraph<PlanetNode> {

    private static final int NODES_TOTAL = 100;
    private static final int NODES_PER_PLANET = 4;
    private static final String TAG = "SS graph";
    public static Heuristic<PlanetNode> heuristic = new Heuristic<PlanetNode>() {
        @Override
        public float estimate(PlanetNode node, PlanetNode endNode) {
            return 0;
        }
    };
    private final PlanetNode[] nodes;
    private final PlanetNode solar;
    private final PlanetNode solarLand;
    private final int sunIndex;
    private float[] costScalar = {.5f, .5f, 1f, 1f, 2f, 3f, 1000f};


    public SolarSystemGraph(){

        nodes = new PlanetNode[SolarSystem.MAX_PLANETS_PER_SYSTEM * NODES_PER_PLANET + SolarSystem.MAX_PLANETS_PER_SYSTEM * SolarSystem.MAX_MOONS_PER_PLANET * NODES_PER_PLANET + NODES_PER_PLANET];
        sunIndex = nodes.length - NODES_PER_PLANET;
        for (int i = 0; i < nodes.length; i++){
            nodes[i] = new PlanetNode(i);

        }
        PathConnection con;

        solar = getSolar(PlanetNode.NodeType.ORBIT);
        solarLand = getSolar(PlanetNode.NodeType.LAND);
        solar.type = PlanetNode.NodeType.ORBIT;
        solarLand.type = PlanetNode.NodeType.LAND;
        int sunScalarIndex = SolarSystem.MAX_PLANETS_PER_SYSTEM;
        con = new PathConnection(solar, solarLand, 100, sunScalarIndex);
        solar.connections.add(con);
        con = new PathConnection(solarLand, solar, 1000, sunScalarIndex);
        solarLand.connections.add(con);


        for (int planet = 0; planet < SolarSystem.MAX_PLANETS_PER_SYSTEM; planet++){
            PlanetNode land = getNode(planet, PlanetNode.NodeType.LAND);
            PlanetNode elliptical = getNode(planet, PlanetNode.NodeType.ELLIPTICAL);
            PlanetNode orbit = getNode(planet, PlanetNode.NodeType.ORBIT);
            if (land.type != null){
                Gdx.app.log(TAG, "FAIL PLANET LAND" + land.type);
            }
            if (orbit.type != null){
                Gdx.app.log(TAG, "FAIL PLANET ORBIT" + land.type);
            }
            if (elliptical.type != null){
                Gdx.app.log(TAG, "FAIL PLANET ELL" + land.type);
            }
            land.type = PlanetNode.NodeType.LAND;
            elliptical.type = PlanetNode.NodeType.ELLIPTICAL;
            orbit.type = PlanetNode.NodeType.ORBIT;

            con = new PathConnection(solar, elliptical, 100, planet);
            solar.connections.add(con);
            con = new PathConnection(elliptical, land, 10, planet);
            elliptical.connections.add(con);
            con = new PathConnection(elliptical, orbit, 100, planet);
            elliptical.connections.add(con);
            con = new PathConnection(orbit, land, 10, planet);
            orbit.connections.add(con);
            con = new PathConnection(land, orbit, 200, planet);
            land.connections.add(con);
            con = new PathConnection(orbit, elliptical, 100, planet);
            orbit.connections.add(con);
            con = new PathConnection(elliptical, solar, 100, planet);
            elliptical.connections.add(con);

            for (int moon = 0; moon < SolarSystem.MAX_MOONS_PER_PLANET; moon++){
                PlanetNode mLand = getNode(planet, moon, PlanetNode.NodeType.LAND);
                PlanetNode mOrbit = getNode(planet, moon, PlanetNode.NodeType.ORBIT);
                if (mLand != null){
                    Gdx.app.log(TAG, "FAIL PLANET mLAND" + land.type);
                }
                if (mOrbit.type != null){
                    Gdx.app.log(TAG, "FAIL PLANET mORBIT" + land.type);
                }
                mOrbit.type = PlanetNode.NodeType.ORBIT;
                mLand.type = PlanetNode.NodeType.LAND;

                con = new PathConnection(elliptical, mOrbit, 5, planet);
                elliptical.connections.add(con);
                con = new PathConnection(mOrbit, mLand, 5, planet);
                mOrbit.connections.add(con);
                con = new PathConnection(mLand, mOrbit, 5, planet);
                mLand.connections.add(con);
                con = new PathConnection(mOrbit, elliptical, 5, planet);
                mOrbit.connections.add(con);
            }
        }
    }

    public PlanetNode getSolar(PlanetNode.NodeType type) {

        int typeIndex = 0;
        switch (type){
            case ELLIPTICAL:typeIndex = 0;break;
            case LAND:typeIndex = 1;break;
            case ORBIT:typeIndex = 2;break;
        }
        return nodes[sunIndex + typeIndex];

    }


    @Override
    public int getIndex(PlanetNode node) {
        return node.index;
    }

    @Override
    public int getNodeCount() {
        return nodes.length;
    }

    @Override
    public Array<Connection<PlanetNode>> getConnections(PlanetNode fromNode) {

        return fromNode.connections;
    }

    public PlanetNode getNode(int planetIndex, int moonIndex, PlanetNode.NodeType type){
        int index = SolarSystem.MAX_PLANETS_PER_SYSTEM + planetIndex * SolarSystem.MAX_MOONS_PER_PLANET + moonIndex;
        int typeIndex = 0;
        switch (type){
            case ELLIPTICAL:typeIndex = 0;break;
            case LAND:typeIndex = 1;break;
            case ORBIT:typeIndex = 2;break;
        }
        return nodes[index * NODES_PER_PLANET + typeIndex];

    }

    public PlanetNode getNode(int planetIndex, PlanetNode.NodeType type){
        int index = planetIndex;
        int typeIndex = 0;
        switch (type){
            case ELLIPTICAL:typeIndex = 0;break;
            case LAND:typeIndex = 1;break;
            case ORBIT:typeIndex = 2;break;
        }
        return nodes[index * NODES_PER_PLANET + typeIndex];

    }

    public PlanetNode getSolar(int currentOrbitalDepth) {
        int typeIndex = currentOrbitalDepth;
        switch (currentOrbitalDepth){
            case GameInfo.ORBIT_ELLIPTICAL: return nodes[sunIndex + 0];
            case GameInfo.ORBIT_LANDED: return nodes[sunIndex + 1];
            case GameInfo.ORBIT_ORBIT: return nodes[sunIndex + 2];
        }
        return null;
    }
    public PlanetNode getNode(int planet, int currentOrbitalDepth) {
        int typeIndex = currentOrbitalDepth;
        switch (currentOrbitalDepth){
            case GameInfo.ORBIT_ELLIPTICAL: return getNode(planet, PlanetNode.NodeType.ELLIPTICAL);
            case GameInfo.ORBIT_LANDED: return getNode(planet, PlanetNode.NodeType.LAND);
            case GameInfo.ORBIT_ORBIT: return getNode(planet, PlanetNode.NodeType.ORBIT);
        }
        return null;
    }

    public PlanetNode getNode(int planet, int moon, int currentOrbitalDepth) {
        int typeIndex = currentOrbitalDepth;
        switch (currentOrbitalDepth){
            case GameInfo.ORBIT_ELLIPTICAL: return getNode(planet, moon,  PlanetNode.NodeType.ELLIPTICAL);
            case GameInfo.ORBIT_LANDED: return getNode(planet, moon, PlanetNode.NodeType.LAND);
            case GameInfo.ORBIT_ORBIT: return getNode(planet, moon, PlanetNode.NodeType.ORBIT);
        }
        return null;
    }

    public class PathConnection implements Connection<PlanetNode>{
        public final PlanetNode from;
        public final PlanetNode to;
        public final int cost;
        private final int planetID;

        public PathConnection(PlanetNode from, PlanetNode to, int cost, int parentPlanet) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.planetID = parentPlanet;
        }

        @Override
        public float getCost() {
            return cost * costScalar[planetID];
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
}
