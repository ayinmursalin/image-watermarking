package core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pseudorandom {

    private final int bound;
    private final double gainFactor;

    public Pseudorandom(int bound, double gainFactor) {
        this.bound = bound;
        this.gainFactor = gainFactor;
    }

    /**
     * create a pseudorandom number sequences with seed (from constructor)
     *
     * @param seed seed to reveal this sequence again if another time generate
     * random
     * @param size number of sequence
     * @return list of pseudorandom numbers
     */
    public List<Double> getRandomNumbers(int seed, int size) {
        Random randomGenerator = new Random(seed);
        ArrayList<Double> sequences = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            sequences.add((randomGenerator.nextInt(bound * 2) - bound) * gainFactor);
        }

        return sequences;
    }

}
