package cn.edu.nju.cs.itrace4.demo.exp.project;

public class Maven_Cluster implements Project{
	public String projectPath = "data/exp/Maven_Cluster/";
	public String rtmClassPath = projectPath + "rtm/RTM_CLASS.txt";
	public String rtmMethodPath = projectPath + "rtm/RTM_Method.txt";
	public String ucPath = projectPath + "uc";
	public String classDirPath = projectPath + "class/code";
	public String class_relationInfoPath = projectPath + "relation/CLASS_relationInfo.ser";
	public String class_relationInfoPathWhole = projectPath + "relation/CLASS_relationInfo_whole.ser";
	public String vsmExpExportPath_ICSME = "data/exp/Maven_Cluster/icsme_result/vsm";
	public String jsExpExportPath_ICSME = "data/exp/Maven_Cluster/icsme_result/js";
	public String lsiExpExportPath_ICSME = "data/exp/Maven_Cluster/icsme_result/lsi";
	public String projectName = "Maven_Cluster";
	
	public String getProjectPath() {
		return projectPath;
	}

	@Override
	public String getRtmClassPath() {
		return rtmClassPath;
	}
	
	@Override
	public String getRtmMethodPath() {
		return rtmMethodPath;
	}
	
	@Override
	public String getUcPath() {
		return ucPath;
	}
	
	@Override
	public String getClassDirPath() {
		return classDirPath;
	}
	
	@Override
	public String getClass_RelationInfoPath() {
		//return class_relationInfoPath;
		return class_relationInfoPathWhole;
	}
	
	@Override
	public String getClass_RelationInfoPathWhole() {
		return class_relationInfoPathWhole;
	}
	
	@Override
	public String getVsmExpExportPath_ICSME() {
		return vsmExpExportPath_ICSME;
	}
	
	@Override
	public String getJsExpExportPath_ICSME() {
		return jsExpExportPath_ICSME;
	}
	
	@Override
	public String getLsiExpExportPath_ICSME() {
		return lsiExpExportPath_ICSME;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getRtmClassPathNew() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassDirPathNew() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethodDirPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVsmExpExportPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJsExpExportPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethod_RelationInfoPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVsmExpExportPath_RE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJsExpExportPath_RE() {
		// TODO Auto-generated method stub
		return null;
	}
}
