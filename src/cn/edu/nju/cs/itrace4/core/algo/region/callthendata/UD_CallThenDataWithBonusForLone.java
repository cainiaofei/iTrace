package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_CallThenDataWithBonusForLone implements CSTI{
	
	protected RelationInfo ri;
	double callThreshold, dataThreshold;
	
	public UD_CallThenDataWithBonusForLone(RelationInfo ri,double callThreshold,
			double dataThreshold){
		this.ri = ri;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		UD_CallSubGraphWithBonusForLone improveBasedCall = new UD_CallSubGraphWithBonusForLone(ri,valid);
		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,matrix);
		
//		afterImproveBasedCall = improveBasedCall.improve(afterImproveBasedCall, textDataset,
//				afterImproveBasedCall);
		
		UD_DataSubGraphWithBonusForLone improveBasedData = new UD_DataSubGraphWithBonusForLone(ri,valid);
		
		return improveBasedData.improve(afterImproveBasedCall, textDataset, afterImproveBasedCall);
	//	return afterImproveBasedCall;
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_CallThenDataWithBonusForLone"+callThreshold+"_"+dataThreshold;
	}

	@Override
	public List<Pair<String, String>> getAlgorithmParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCorrectImprovedTargetsList() {
		// TODO Auto-generated method stub
		return null;
	}

}
