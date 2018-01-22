package cn.edu.nju.cs.refactor.exp.input;

import cn.edu.nju.cs.refactor.util.Tool;

public interface Factory extends Tool{
	public Object generate(String name);
}
