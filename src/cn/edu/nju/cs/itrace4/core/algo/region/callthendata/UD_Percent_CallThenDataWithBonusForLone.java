package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.List;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_Percent_CallThenDataWithBonusForLone implements CSTI{
	
	protected RelationInfo ri;
	double callThreshold, dataThreshold;
	
	public UD_Percent_CallThenDataWithBonusForLone(RelationInfo ri,double callThreshold,
			double dataThreshold){
		this.ri = ri;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		UD_Percent_CallSubGraphWithBonusForLone improveBasedCall = new UD_Percent_CallSubGraphWithBonusForLone(ri,0.5);
		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,matrix);
		
		UD_Percent_DataSubGraphWithBonusForLone improveBasedData = new UD_Percent_DataSubGraphWithBonusForLone(ri,0.5);
		
		return improveBasedData.improve(afterImproveBasedCall, textDataset, afterImproveBasedCall);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_Percent_CallThenDataWithBonusForLone"+callThreshold+"_"+dataThreshold;
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
