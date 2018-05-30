package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

import java.util.Comparator;


public class CodeRegionPairCmp implements Comparator<CodeRegionPair>{

	@Override
	public int compare(CodeRegionPair former, CodeRegionPair latter) {
		double diff = former.getCloseness()-latter.getCloseness();
		if(diff<0) {
			return 1;
		}
		else if(diff>0) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
