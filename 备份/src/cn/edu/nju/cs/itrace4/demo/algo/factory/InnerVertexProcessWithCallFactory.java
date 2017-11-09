package cn.edu.nju.cs.itrace4.demo.algo.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.InnerVertexProcessWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.MeanAllocateWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.WeightAverageWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class InnerVertexProcessWithCallFactory {
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	private int amountNeedToJudge;
	//store have judge in order to calculate rate which need to judge by user
	private Map<String,Set<String>> judged;
	private double[][] graphs;
	
	public InnerVertexProcessWithCallFactory(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			double[][] graphs){
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.amountNeedToJudge = amountNeedToJudge;
		this.judged = judged;
		this.graphs = graphs;
	}
	
	public InnerVertexProcessWithCallSubGraph getInnerVertexProcessWithCallSubGraphObj(Class<?> method){
		if(method.equals(MeanAllocateWithCallSubGraph.class)){
			return new MeanAllocateWithCallSubGraph(vertexIdNameMap,callSubGraphList,amountNeedToJudge,
					judged);
		}
		else if(method.equals(WeightAverageWithCallSubGraph.class)){
			return new WeightAverageWithCallSubGraph(vertexIdNameMap,callSubGraphList,amountNeedToJudge,
					judged,graphs);
		}
		else {
			return null;
		}
	}
}
