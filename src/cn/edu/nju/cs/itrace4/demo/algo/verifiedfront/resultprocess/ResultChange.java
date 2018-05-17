package cn.edu.nju.cs.itrace4.demo.algo.verifiedfront.resultprocess;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class ResultChange {
	public static void modifyResult(Result result,Map<String,Set<String>> verifiedLinkList) {
		
		for(String req:verifiedLinkList.keySet()) {
			Set<String> classSet = verifiedLinkList.get(req);
			for(String className : classSet) {
				result.matrix.setScoreForLink(req, className, 1.0);
			}
		}
//		for(SingleLink singleLink:result.matrix.allLinks()) {
//			String req = singleLink.getSourceArtifactId();
//			String className = singleLink.getTargetArtifactId();
//			if(verifiedLinkList.containsKey(req) && verifiedLinkList.get(req).contains(className)) {
//				result.matrix.setScoreForLink(req, className, 1.0);
//			}
//		}
	}
}
