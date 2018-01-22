package cn.edu.nju.cs.refactor.exp.input;
/**
 * @date 2018.1.22
 * @author zzf
 * @description  factory pattern.
 */
public interface ModelFactory extends Factory{
	public String generate(String modelName);
}
