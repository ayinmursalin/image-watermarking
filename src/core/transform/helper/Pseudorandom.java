package core.transform.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pseudorandom {

    private final int bound;

    public Pseudorandom(int bound) {
        this.bound = bound;
    }
    
    

    /**
     * create a pseudorandom number sequences with seed (from constructor)
     * 
     * @param seed seed to reveal this sequence again if another time generate random
     * @param size number of sequence
     * @return list of pseudorandom numbers
     */
    public List<Integer> getRandomNumbers(int seed, int size) {
        Random randomGenerator = new Random(seed);
        ArrayList<Integer> sequences = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            sequences.add(randomGenerator.nextInt(bound));
        }

        return sequences;
    }

}
