package cn.edu.nju.cs.itrace4.core.algo.region.util.graph;

public class UnDirectedGraph extends GraphImp{
	
	private double[][] graphs;
	private int routerMaxLen;

	public UnDirectedGraph(double[][] graphs,int routerMaxLen) {
		super(graphs,routerMaxLen);
		this.graphs = graphs;
		this.routerMaxLen = routerMaxLen;
	}
	
	@Override
	protected boolean existRouterBetweenCurAndNext(int current, int next) {
		return graphs[current][next]>0 || graphs[next][current]>0;
	}
	
}
