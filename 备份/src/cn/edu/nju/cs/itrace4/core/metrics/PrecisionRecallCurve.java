package cn.edu.nju.cs.itrace4.core.metrics;

import java.util.LinkedHashMap;

/**
 * Created by niejia on 15/2/23.
 */
public class PrecisionRecallCurve extends LinkedHashMap<String, Double> {
    private String name;
    private double cutParameter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCutParameter() {
        return cutParameter;
    }

    public void setCutParameter(double cutParameter) {
        this.cutParameter = cutParameter;
    }
}
