package ninja.trek;

public interface RandomnessSource {

	long nextLong();

	RandomnessSource copy();

	int next(int bits);

}
