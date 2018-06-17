package cn.edu.nju.cs.itrace4.exp.input;

import cn.edu.nju.cs.itrace4.demo.exp.project.Project;

public interface ProjectFactory extends Factory{
	public Project generate(String projectName);
}
