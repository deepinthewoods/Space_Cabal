package ninja.trek;

public class Planet {

	private int seed;
	int index;
	public int parent = -1;
	public int parentOrder = -1;
	
	public Planet(int random, int index) {
		seed = random;
		this.index = index;
	}

	public String toString(){
		String s = "pPanet " + index;
		return s;
	}
}
