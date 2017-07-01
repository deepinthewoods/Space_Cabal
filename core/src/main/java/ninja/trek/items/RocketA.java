package ninja.trek.items;

import ninja.trek.WeaponItem.WeaponType;

public class RocketA extends MissileItem {

	public RocketA() {
		super("Rocket A", "Standard Rocket");
		cost = 10000;
		fireWindDownTime = .8f;
		chargeDrawLimit = 100;
		weaponType = WeaponType.missile;
		variantIndex = 0;
	}

}
