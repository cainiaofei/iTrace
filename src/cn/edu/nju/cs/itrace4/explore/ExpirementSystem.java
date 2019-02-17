package cn.edu.nju.cs.itrace4.explore;

public class ExpirementSystem {
	private String ucPath;
	private String rtmClassPath;
	private String class_relationInfoPath;
	private String classDirPath;
	private String projectName;
	
	public ExpirementSystem(String ucPath,String rtmClassPath,String class_relationInfoPath,
			String classDirPath,String projectName){
		this.ucPath = ucPath;
		this.rtmClassPath = rtmClassPath;
		this.class_relationInfoPath = class_relationInfoPath;
		this.classDirPath = classDirPath;
		this.projectName = projectName;
	}
	
	public String getProjectName(){
		return projectName;
	}
	
	public String getUcPath() {
		return ucPath;
	}
	
	public String getRtmClassPath() {
		return rtmClassPath;
	}
	
	public String getClass_relationInfoPath() {
		return class_relationInfoPath;
	}
	
	public String getClassDirPath() {
		return classDirPath;
	}
}
