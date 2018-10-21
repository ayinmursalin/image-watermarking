package core.transform.helper;

import java.util.ArrayList;
import java.util.List;

public class PearsonCorrelation {

    private final ArrayList<Integer> x;
    private final ArrayList<Integer> y;

    public PearsonCorrelation(ArrayList<Integer> x, ArrayList<Integer> y) {
        this.x = x;
        this.y = y;
    }

    public double getCorrelation() throws ArrayIndexOutOfBoundsException {
        if (x.size() != y.size()) {
            throw new ArrayIndexOutOfBoundsException("V1 and V2 should have the same length");
        }

        int n = x.size();
        double sumProduct = countProductSum();
        double sumX = countSum(x);
        double sumY = countSum(y);
        double sumSquaredX = countSquaredSum(x);
        double sumSquaredY = countSquaredSum(y);

        double fx = (n * sumSquaredX) - Math.pow(sumX, 2);
        double fy = (n * sumSquaredY) - Math.pow(sumY, 2);

        double fa = (n * sumProduct) - (sumX * sumY);
        double fb = Math.sqrt(fx * fy);
        
        return fa / fb;
    }

    private double countProductSum() {
        double sum = 0.0;

        // x size == y size
        for (int i = 0; i < x.size(); i++) {
            double product = x.get(i) * y.get(i);

            sum += product;
        }

        return sum;
    }

    private double countSum(List<Integer> values) {
        double sum = 0.0;

        for (Integer v : values) {
            sum += v;
        }

        return sum;
    }

    private double countSquaredSum(List<Integer> values) {
        double sum = 0.0;

        for (Integer v : values) {
            // v^2
            double squared = Math.pow(v, 2);

            sum += squared;
        }

        return sum;
    }

}
