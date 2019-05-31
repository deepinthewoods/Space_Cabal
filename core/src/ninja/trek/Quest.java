package ninja.trek.gen;

import com.badlogic.gdx.utils.Array;

import ninja.trek.gen.Planet.Type;
import ninja.trek.gen.QuestOption.QuestSpawnRequirement;

public class Quest {
	public String name;
	public String[] commands;
	public String text = "Explanation text";
	public String[] tags = {};
	public boolean isOneOff = false;
	public QuestSpawnRequirement[] requiredAny = {};
	public Array<QuestOption> options = new Array<QuestOption>();
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean isValidFor(Planet planet) {
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
		
		return valid;
		
	}
}
