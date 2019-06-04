package ninja.trek;

public class Item {

	public String name;
	public CharSequence description;
	public int cost = 10;
	public String icon;

	public Item(String name, String desc) {
		description = desc;
		this.name = name;
	}



}
