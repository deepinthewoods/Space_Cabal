package ninja.trek;

public class Planet {

	private int seed;
	private int index;
	
	public Planet(int random, int index) {
		seed = random;
		this.index = index;
	}

	public String toString(){
		String s = "pPanet " + index;
		return s;
	}
}
