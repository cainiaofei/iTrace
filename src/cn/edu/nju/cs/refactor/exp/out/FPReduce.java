package cn.edu.nju.cs.refactor.exp.out;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.metrics.Result;
/**
 * @date 2018.1.22
 * @author zzf
 * @description 
 */
public interface FPReduce {
	//
	public String[] getFPReduceData(Result ours,Result target,
			Map<String,Set<String>> visited);
}
