package ninja.trek.items;

public class RocketB extends MissileItem {

	public RocketB() {
		super("Rocket B", "Standard Rocket");
		cost = 100000;
		fireWindDownTime = .1f;
		chargeDrawLimit = 100;
		weaponType = WeaponType.missile;
		variantIndex = 1;
	}

}
