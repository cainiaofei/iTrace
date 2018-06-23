package cn.edu.nju.cs.itrace4.explore;

public class Gantt extends ExpirementSystem{
	private static String ucPath = "data/exp/Gantt/uc";
	private static String rtmClassPath = "data/exp/Gantt/rtm/RTM_CLASS.txt";
	private static String class_relationInfoPath = "data/exp/Gantt/relation/Class_relationInfo.ser";
	private static String classDirPath = "data/exp/Gantt/class/code";
	private static String projectName = "Gantt";
	
	public Gantt(){
		super(ucPath,rtmClassPath,class_relationInfoPath,
				classDirPath,projectName);
	}
	
}
