package cn.edu.nju.cs.itrace4.visual;

import java.util.TreeSet;

/**
 * Created by niejia on 15/4/22.
 */
public class Main {
    public static void main(String[] args) {
//        DirectedSparseGraph<Integer, Integer> graph = new DirectedSparseGraph<>();
//
//        graph.addVertex(0);
//        graph.addVertex(1);
//        graph.addVertex(2);
//
//        graph.addEdge(0, 0, 1);
//        graph.addEdge(1, 1, 2);
//        System.out.println(graph.isNeighbor(0, 2));
//        System.out.println(graph.getIncidentEdges(2));
//        System.out.println(graph.getSuccessors(0));

//        System.out.println(graph.getIncidentCount( )

//        System.out.println(Math.log(22 / (1.0 * 1)));

        TreeSet<Double> bounus = new TreeSet<>();
        bounus.add(0.8);
        bounus.add(0.4);
        bounus.add(0.0);
        bounus.add(0.9);
        System.out.println(bounus.last());
    }
}

