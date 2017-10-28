package cn.edu.nju.cs.itrace4.demo.getPaperData;

import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;

public class GetLineChartData {
	private double percent;
	private String basePath;
	private Project[] projects = new Project[3];
	private String[] models = new String[3];
	private double callThreshold = 0.4, dataThreshold = 0.7;
	
	public GetLineChartData(String basePath) {
		this.basePath = basePath;
		initProjects(projects);
		initModels(models);
	}
	
	public void initProjects(Project[] project){
		project[0] = new Itrust();
		project[1] = new Maven();
		project[2] = new Infinispan();
	}
	
	public void initModels(String[] models){
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
	}
	
	public void getLineData() {
		
		
	}
	
	public static void main(String[] args) {
		String basePath = "./";
		GetLineChartData getLineChartData = new GetLineChartData();
		
	}
}
