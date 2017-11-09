package cn.edu.nju.cs.itrace4.util;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by niejia on 16/3/27.
 */
public class PauTa {

    private Set<Double> exceptionValue;
//    private Set<Double> prettyBigValues;
//    private Set<Double> prettySmallValues;

    public PauTa(double[] values) {
        exceptionValue = new LinkedHashSet<>();

        Mean mean = new Mean();
        double meanValue = mean.evaluate(values);
//        System.out.println(" meanValue = " + meanValue);
        double bessel = bessel(values, meanValue);
//        System.out.println(" bessel = " + bessel);

        double threeAlpha = 3.0 * bessel;
//        System.out.println(" threeAlpha = " + threeAlpha);
        for (double v : values) {
            double diff = Math.abs(v - meanValue);
//            System.out.println(" diff = " + diff);
            if (diff > threeAlpha) {
                exceptionValue.add(v);
            }
        }

//        System.out.println(" exceptionValue = " + exceptionValue);
    }

    private double bessel(double[] values, double meanValue) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            double square = Math.pow(values[i] - meanValue, 2);
            sum += square;
        }

        double temp = sum / ((double) values.length - 1);
        return Math.sqrt(temp);
    }

    public Set<Double> getExceptionValue() {
        return exceptionValue;
    }

    public static void main(String[] args) {
        double[] array = new double[10];
        array[0] = 1.01;
        array[1] = 1.00;
        array[2] = 1.03;
        array[3] = 1.02;
        array[4] = 6.05;
        array[5] = 1.03;
        array[6] = 1.05;
        array[7] = 1.02;
        array[8] = 1.01;
        array[9] = 1.02;

        PauTa pauTa = new PauTa(array);

    }
}
