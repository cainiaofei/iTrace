package cn.edu.nju.cs.itrace4.core.metrics;

import java.util.List;

/**
 * Created by niejia on 16/1/27.
 */
public class CompareListAtDifferentRecall {
    public static void computePrecisionDiff(List<Double> treatment, List<Double> control) {
        int recall = 10;
        for (int i = 0; i < treatment.size(); i++) {
            double diff = treatment.get(i) - control.get(i);
            System.out.println(diff + " @Recall: " + recall);
            recall += 10;
        }
    }

    public static void computeFPDiff(List<Integer> treatment, List<Integer> control) {
        int recall = 10;
        for (int i = 0; i < treatment.size(); i++) {
            int diff = treatment.get(i) - control.get(i);
            System.out.println(diff + " @Recall: " + recall);
            recall += 10;
        }
    }
}
