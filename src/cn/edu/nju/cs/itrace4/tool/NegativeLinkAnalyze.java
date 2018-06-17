package cn.edu.nju.cs.itrace4.tool;

import java.util.Collections;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class NegativeLinkAnalyze implements ResultAnalyze{

	@Override
	public void findNegativeLink(Result result) {
		SimilarityMatrix oracle = result.getOracle();
		SimilarityMatrix candidateMatrix = result.getMatrix();
		LinksList links = candidateMatrix.getLinksAboveThreshold();
		int count = 0;
		int rank = 1;
        Collections.sort(links, Collections.reverseOrder());
        for (SingleLink link : links) {
            if (oracle.isLinkAboveThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
            	;
            } else {
            	String req = link.getSourceArtifactId();
            	String className = link.getTargetArtifactId();
            	count++;
            	System.out.println("rank:"+rank+"...."+req+"----->"+className+"......"+"count:"+count);
            }
            rank++;
        }
	}
	
}
