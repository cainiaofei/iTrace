package cn.edu.nju.cs.itrace4.demo.algo.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.InnerVertexProcessWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.MeanAllocateWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.WeightAverageWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class InnerVertexProcessWithDataFactory {
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> dataSubGraphList;
	private int amountNeedToJudge;
	//store have judge in order to calculate rate which need to judge by user
	private Map<String,Set<String>> judged;
	private double[][] graphs;
	
	public InnerVertexProcessWithDataFactory(Map<Integer,String> vertexIdNameMap,List<SubGraph> dataSubGraphList,
			double[][] graphs){
		this.vertexIdNameMap = vertexIdNameMap;
		this.dataSubGraphList = dataSubGraphList;
		this.graphs = graphs;
	}
	
	public InnerVertexProcessWithDataSubGraph getInnerVertexProcessWithDataSubGraphObj(Class<?> method){
		if(method.equals(MeanAllocateWithDataSubGraph.class)){
			return new MeanAllocateWithDataSubGraph(vertexIdNameMap,dataSubGraphList,amountNeedToJudge,judged);
		}
		else if(method.equals(WeightAverageWithDataSubGraph.class)){
			return new WeightAverageWithDataSubGraph(vertexIdNameMap,dataSubGraphList,
					amountNeedToJudge,judged,graphs);
		}
		else{
			return null;
		}
	}
	
}
