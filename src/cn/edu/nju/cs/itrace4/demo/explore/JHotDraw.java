package cn.edu.nju.cs.itrace4.demo.explore;

public class JHotDraw extends ExpirementSystem{
	private static String ucPath = "data/exp/JHotDraw/uc";
	private static String rtmClassPath = "data/exp/JHotDraw/rtm/RTM_CLASS.txt";
	private static String class_relationInfoPath = "data/exp/JHotDraw/relation/Class_relationInfo.ser";
	private static String classDirPath = "data/exp/JHotDraw/class/code";
	private static String projectName = "JHotDraw";
	
	public JHotDraw(){
		super(ucPath,rtmClassPath,class_relationInfoPath,
				classDirPath,projectName);
	}
	
}
