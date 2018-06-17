package cn.edu.nju.cs.itrace4.exp.input;

import cn.edu.nju.cs.itrace4.util.Tool;

public interface Factory extends Tool{
	public Object generate(String name);
}
