package ninja.trek.items;

public class LaserA extends LaserItem {



	public LaserA() {
		super("laser A", "laser that shoots lasers");
		cost = 100;
        chargeDrawLimit = 50;
		fireWindDownTime = 0f;
        shieldPiercing = 1;
        damage = 30;
        icon = "pistol-gun";
	}

}
