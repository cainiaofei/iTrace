package cn.edu.nju.cs.itrace4.exp.input;

import cn.edu.nju.cs.itrace4.util.FileParse.project.Infinispan;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Itrust;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Maven_TestCase;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Pig;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;

public class ProjectFactoryImp implements ProjectFactory{

	@Override
	public Project generate(String projectName) {
		Project project = null;
		if(projectName.compareToIgnoreCase("iTrust")==0) {
			project = new Itrust();
		}else if(projectName.compareToIgnoreCase("Maven_TestCase")==0) {
			project = new Maven_TestCase();
		}else if(projectName.compareToIgnoreCase("Infinispan")==0) {
			project = new Infinispan();
		}else if(projectName.compareToIgnoreCase("Pig")==0) {
			project = new Pig();
		}else {
			System.out.println("there don't exist this type object ----cn.edu.nju.cs."
					+ "refactor.exp.input.ProjectFactoryImp ");
		}
		return project;
	}

	@Override
	public String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("generate project object base on project name");
		return null;
	}

}
