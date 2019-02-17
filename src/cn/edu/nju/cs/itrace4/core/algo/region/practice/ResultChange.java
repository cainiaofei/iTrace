package cn.edu.nju.cs.itrace4.core.algo.region.practice;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class ResultChange {
	public static void modifyResult(Result result,Map<String,Set<String>> verifiedLinkList) {
		
		for(String req:verifiedLinkList.keySet()) {
			Set<String> classSet = verifiedLinkList.get(req);
			for(String className : classSet) {
				result.matrix.setScoreForLink(req, className, 1.0);
			}
		}
	}
}
