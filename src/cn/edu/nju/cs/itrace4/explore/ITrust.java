package cn.edu.nju.cs.itrace4.explore;

public class ITrust extends ExpirementSystem{
	private static String ucPath = "data/exp/iTrust/uc";
	private static String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
	private static String class_relationInfoPath = "data/exp/iTrust/relation/Class_relationInfo.ser";
	private static String classDirPath = "data/exp/iTrust/class/code";
	private static String projectName = "iTrust";
	
	public ITrust(){
		super(ucPath,rtmClassPath,class_relationInfoPath,
				classDirPath,projectName);
	}
	
}
