package cn.edu.nju.cs.refactor.util;

/**
 * @author zzf
 * @date 2018.01.19
 * @description design pattern: less knowledge. I don't know any information about the class which 
 *   invoke me. 
 */
public interface Tool {
	// print the what this class do.
	public String description();
}
