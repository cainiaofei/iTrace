package cn.edu.nju.cs.itrace4.util;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * Created by niejia on 16/3/24.
 */
public class ZScore {

    public static double compute(double v, double mean, double standardDeviation) {
        double zScore = (v - mean) / standardDeviation;
        return zScore;
    }

    public static void main(String[] args) {
        double[] v = {1.0, 2.0, 3.0, 4.0, 5.0,6.0,5.0};

        Mean mean = new Mean();
//        System.out.println(mean.evaluate(v));

        StandardDeviation standardDeviation = new StandardDeviation();
//        System.out.println(standardDeviation.evaluate(v));

        for (double d : v) {
            System.out.println(compute(d, mean.evaluate(v), standardDeviation.evaluate(v)));
        }
    }

}
