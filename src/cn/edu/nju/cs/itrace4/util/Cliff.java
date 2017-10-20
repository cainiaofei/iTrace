package cn.edu.nju.cs.itrace4.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 16/1/23.
 */ 

public class Cliff {

    public static double delta(List<Double> treatment, List<Double> control) {
        int tGEc = 0;
        int cGEt = 0;

        for (int i = 0; i < treatment.size(); i++) {
            for (int j = 0; j < control.size(); j++) {
                double t = treatment.get(i);
                double c = control.get(j);
                if (t > c) {
                    tGEc++;
                }

                if (c > t) {
                    cGEt++;
                }
            }
        }

        double result = 1.0 * Math.abs(tGEc - cGEt) / (1.0 * (treatment.size() * control.size()));
        return result;
    }

    public static void main(String[] args) {
        List<Double> treatment = new ArrayList<Double>();
        List<Double> control = new ArrayList<Double>();
        treatment.add(1.0);
        treatment.add(2.0);
        treatment.add(3.0);
        treatment.add(4.0 );
        treatment.add(5.0);
        treatment. add(6.0);

        control.add(3.0);
        control.add(4.0);
        control.add(5.0);
        control.add(6.0);
        control.add(7.0);
        control.add(8.0);

        System.out.println(delta(treatment, control));
    }
}

