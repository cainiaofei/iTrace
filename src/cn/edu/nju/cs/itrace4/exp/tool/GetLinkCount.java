package cn.edu.nju.cs.itrace4.exp.tool;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class GetLinkCount {
	public static int getResultSize(SimilarityMatrix matrix) {
		int count = 0;
		for(String key:matrix.sourceArtifactsIds()) {
			count += matrix.getLinksForSourceId(key).size();
		}
		return count;
	}
}
