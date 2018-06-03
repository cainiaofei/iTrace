package cn.edu.nju.cs.itrace4.demo.algo.util.graph;

public class DirectedGraph extends GraphImp{
	private double[][] graphs;
	private int routerMaxLen;

	public DirectedGraph(double[][] graphs,int routerMaxLen) {
		super(graphs,routerMaxLen);
		this.graphs = graphs;
		this.routerMaxLen = routerMaxLen;
	}
	
	@Override
	protected boolean existRouterBetweenCurAndNext(int current, int next) {
		return graphs[current][next]>0;
	}

}
