package ninja.trek;

import com.badlogic.gdx.utils.Array;

import ninja.trek.gen.Planet;
import ninja.trek.gen.Planet.Type;
import ninja.trek.QuestOption.QuestSpawnRequirement;

public class Quest {
	public String name;
	public String[] commands;
	public String text = "Explanation text";
	public String[] tags = {};
	public boolean isOneOff = false;
	public QuestSpawnRequirement[] requiredAny = {};
	public PlanetNode.NodeType[] orbits = {PlanetNode.NodeType.ORBIT};
	public Array<QuestOption> options = new Array<QuestOption>();
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean isValidFor(Planet planet, PlanetNode.NodeType orbit) {
		boolean valid = false;
		for (QuestSpawnRequirement req : requiredAny) {
			switch (req) {
			case EARTH_LIKE:
				if (planet.planetType == Type.EARTH_LIKE) valid = true;
				break;
			case MARS_LIKE:
				if (planet.planetType == Type.MARS_LIKE) valid = true;
				break;
			case MOON:
				if (planet.planetType == Type.MOON) valid = true;
				break;
			case SUN:
				if (planet.planetType == Type.STAR) valid = true;
				break;
				case GAS_GIANT:
					if (planet.planetType == Type.GAS_GIANT) valid = true;
					break;
				case METEOR:
					if (planet.planetType == Type.METEOR) valid = true;
					break;
				case INNER_PLANET:
					if (planet.planetType == Type.INNER) valid = true;
					break;
			}
		}
		boolean orbitValid = false;

		for (int i = 0; i < orbits.length; i++){
			if (orbits[i] == orbit) orbitValid = true;
		}

		
		return valid && orbitValid;
		
	}

	public boolean isValidForPlaying(Planet planet, PlanetNode.NodeType orbit) {
		return isValidFor(planet, orbit);
	}
}
