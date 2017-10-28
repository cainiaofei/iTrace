package cn.edu.nju.cs.itrace4.core.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/3/16.
 */
public class Overlap {
    public static void compare(String nameA, List<String> targetsListA, String nameB, List<String> targetsListB) {
        List<String> overlap = new ArrayList<>();

        for (String s : targetsListA) {
            if (targetsListB.contains(s) && !overlap.contains(s)) {
                overlap.add(s);
            }
        }

        StringBuilder overlapBuffer = new StringBuilder();
        overlapBuffer.append("Overlap: " + overlap.size() + "\n");
        for (String s : overlap) {
            overlapBuffer.append(s);
            overlapBuffer.append(" ");
        }
        overlapBuffer.append("\n");

        StringBuilder uniqueTargetsInA = new StringBuilder();
        uniqueTargetsInA.append(nameA + " Unique: \n");
        for (String s : targetsListA) {
            if (!overlap.contains(s)) {
                uniqueTargetsInA.append(s);
                uniqueTargetsInA.append(" ");
            }
        }
        uniqueTargetsInA.append("\n");

        StringBuilder uniqueTargetsInB = new StringBuilder();
        uniqueTargetsInB.append(nameB + " Unique: \n");
        for (String s : targetsListB) {
            if (!overlap.contains(s)) {
                uniqueTargetsInB.append(s);
                uniqueTargetsInB.append(" ");
            }
        }
        uniqueTargetsInA.append("\n");

        System.out.println(overlapBuffer.toString());
        System.out.println(uniqueTargetsInA.toString());
        System.out.println(uniqueTargetsInB.toString());
    }
}
