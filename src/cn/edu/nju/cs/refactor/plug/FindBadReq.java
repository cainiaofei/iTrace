package cn.edu.nju.cs.refactor.plug;

import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class FindBadReq implements Plug{
	private int countThreshold = 1;
	
	@Override
	public void detect(Result ours, Result opp) {
		SimilarityMatrix oracle = ours.getOracle();
		int number = getReqCountBySpecifiedClass(oracle);
		
		System.out.println("the number of requirement which implemented by " + countThreshold+" class: " + number);
		
		Map<String,Double> ourPR = ours.getAveragePrecisionByQuery();
		Map<String,Double> oppPR = opp.getAveragePrecisionByQuery();
		
		int countWin = 0;
		int countFailure = 0;
		for(String req:ourPR.keySet()) {
			double ourPrecision = ourPR.get(req);
			double oppPrecision = oppPR.get(req);
			double diff = ourPrecision - oppPrecision;
			if(diff<0) {
				countFailure++;
				int count = oracle.getLinksAboveThresholdForSourceArtifact(req).size();
				System.out.println(req+"------"+count+"  diff:"+diff);
			}
			if(diff>0) {
				countWin++;
			}
		}
		
		System.out.println("countWin:"+countWin);
		System.out.println("countFailure:"+countFailure);
		System.out.println("\n***********************************************************");
		
		System.out.println("our map:"+ours.getMeanAveragePrecisionByQuery());
		System.out.println("their map:"+opp.getMeanAveragePrecisionByQuery());
	}

	private int getReqCountBySpecifiedClass(SimilarityMatrix oracle) {
		int count = 0;
		LinksList allLinks = oracle.allLinks();
		for(SingleLink link:allLinks) {
			String req = link.getSourceArtifactId();
			if(oracle.getCountOfLinksAboveThresholdForSourceArtifact(req)==1) {
				count++;
			}
		}
		return count;
	}
	
}
