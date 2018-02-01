package cn.edu.nju.cs.refactor.plug;

import cn.edu.nju.cs.itrace4.core.metrics.Result;

public interface Plug {
	public void detect(Result ours,Result opp);
}
