package cn.edu.nju.cs.itrace4.demo.algo.util.graph;

import java.util.ArrayList;
import java.util.List;

public abstract class GraphImp implements Graph{
	private double[][] graphs;
	private int routerMaxLen;
	
	public GraphImp(double[][] graphs,int routerMaxLen) {
		this.graphs = graphs;
		this.routerMaxLen = routerMaxLen;
	}
	
	@Override
	public List<List<Integer>> getAllRoutersBetweenTwoVertex(int vertexA, int vertexB) {
		List<Integer> curRouter = new ArrayList<Integer>();
		curRouter.add(vertexA);
		return getValidRouters(curRouter,vertexA,vertexB);
	}

	/**
	 * @param curRouter the router by now, maybe not valid unless the end vertex is end
	 */
	private List<List<Integer>> getValidRouters(List<Integer> curRouter, int current,
			int end) {
		List<List<Integer>> allRouters = new ArrayList<List<Integer>>();
		if(current==end) {
			allRouters.add(new ArrayList<Integer>(curRouter));
		}
		else if(curRouter.size()>routerMaxLen) {
			return allRouters;
		}
		else {
			//the id of vertex start from 1.
			for(int i = 1; i < graphs.length;i++) {
				if(!curRouter.contains(i) && existRouterBetweenCurAndNext(current,i)) {
					curRouter.add(i);
					allRouters.addAll(getValidRouters(curRouter,i,end));
					curRouter.remove(curRouter.size()-1);
				}
			}
		}
		return allRouters;
	}

	/**
	 * this method maybe different based on whether the graph is directed.
	 */
	protected abstract boolean existRouterBetweenCurAndNext(int current, int next);
}
