package cn.edu.nju.cs.refactor.exp.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class FPReduceThinkVisitBasedOnPrecision implements FPReduce{
	@Override
	public String[] getFPReduceData(Result ours, Result target, Map<String, Set<String>> visited) {
		List<Double> oursFP = ours.getPrecisionAtRecallByTen(visited);
		List<Double> compareFP = target.getPrecisionAtRecallByTen();
		
	    String[] res = new String[oursFP.size()];
	    for(int i = 0; i < oursFP.size();i++) {
	    	double diff = oursFP.get(i) - compareFP.get(i);
	    	res[i] = String.format("%.2f",diff*100);
	    	if(diff>0) {
	    		res[i] = "+" + res[i] + "%";
	    	}
	    	else {
	    		res[i] = res[i] + "%";
	    	}
	    }
		return res;
	}

}
