package ninja.trek;

public class WeaponItem extends Item {

	public enum WeaponType {laser, missile};
	public WeaponType weaponType = WeaponType.laser;
	public int cost = 500;
	public float fireWindDownTime = .5f;
	public int chargeDrawLimit = 100;
	public int variantIndex;
    public int damage = 6000;
    public int shieldDamage;

	public WeaponItem(String name, String desc) {
		super(name, desc);
	}

}
