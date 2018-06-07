package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class UD_CallSubGraph_Then_DataSubGraph_ClosenessWithPenalty extends
					UD_CallSubGraph_Then_DataSubGraph_Closeness{

	public UD_CallSubGraph_Then_DataSubGraph_ClosenessWithPenalty(double callThreshold, double dataThreshold,
			Map<Integer, String> vertexIdNameMap, RelationInfo ri) {
		super(callThreshold, dataThreshold, vertexIdNameMap, ri);
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		List<SubGraph> callSubGraph = storeCallSubGraph.getSubGraphs(ri);
		UD_SubGraph_Closeness improveBasedCall = new UD_SubGraph_ClosenessWithPenalty(callThreshold,dataThreshold,
				callSubGraph,vertexIdNameMap);
		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,matrix);
		
		List<SubGraph> dataSubGraph = storeDataSubGraph.getSubGraphs(ri);
		UD_SubGraph_Closeness improveBasedData = new UD_SubGraph_ClosenessWithPenalty(callThreshold,dataThreshold,
				dataSubGraph,vertexIdNameMap);
		
		return improveBasedData.improve(afterImproveBasedCall, textDataset, afterImproveBasedCall);
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "Call_Then_Data_WithPenalty"+callThreshold+","+dataThreshold;
	}
	
}
