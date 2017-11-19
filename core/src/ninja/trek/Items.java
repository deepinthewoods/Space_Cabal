package ninja.trek;

import ninja.trek.items.LaserA;
import ninja.trek.items.RocketA;
import ninja.trek.items.RocketB;

public class Items {
private static final int MAX_ITEMS = 100;

public static int 
	laser1 = 0
	, laser2 = 1
	, rocket1 = 2
	, rocket2 = 3;


private static Item[] defs = new Item[MAX_ITEMS];

private static Item defaultItem;

public static void init(){
	defs[laser1] = new LaserA();
	defs[rocket1] = new RocketA();
	defs[rocket2] = new RocketB();
			
			
	defaultItem = new Item("defname", "default item"){
		
	};
}

public static Item getDef(int i){
	Item item = defs[i];
	if (item == null) return defaultItem;
	return item;
}
}
