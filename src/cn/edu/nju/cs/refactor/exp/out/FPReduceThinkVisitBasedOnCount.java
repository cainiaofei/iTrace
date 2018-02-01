package cn.edu.nju.cs.refactor.exp.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.metrics.Result;
/**
 * @date 2018.1.22
 * @author zzf
 * @description ~~ 
 */
public class FPReduceThinkVisitBasedOnCount implements FPReduce{

	@Override
	public String[] getFPReduceData(Result ours, Result target,
			Map<String, Set<String>> visited) {
		List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen(visited);
	    List<Integer> compareFP = target.getFalsePositiveAtRecallByTen();
	    String[] res = new String[oursFP.size()];
	    for(int i = 0; i < oursFP.size();i++) {
	    	int diff = oursFP.get(i) - compareFP.get(i);
	    	if(diff>0) {
	    		res[i] = "+" + diff;
	    	}
	    	else {
	    		res[i] = "" + diff;
	    	}
	    }
		return res;
	}

}
