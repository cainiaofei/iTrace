package cn.edu.nju.cs.itrace4.demo.exp.project;

public class Itrust implements Project{
	public static String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
	public static String rtmMethodPath = "data/exp/iTrust/rtm/RTM_Method.txt";
    public static String ucPath = "data/exp/iTrust/uc";
    public static String classDirPath = "data/exp/iTrust/class/code";
    public static String methodDirPath = "data/exp/iTrust/method/code";
    public static String vsmExpExportPath = "data/exp/iTrust/result/vsm";
    public static String vsmExpExportPath_RE = "data/exp/iTrust/result/vsm_re";
    public static String jsExpExportPath = "data/exp/iTrust/result/js";
    public static String jsExpExportPath_RE = "data/exp/iTrust/result/js_re";
    public static String method_relationInfoPath = "data/exp/iTrust/relation/METHOD_relationInfo.ser";
    public static String class_relationInfoPath = "data/exp/iTrust/relation/CLASS_relationInfo_whole.ser";
    public static String class_relationInfoPathWhole = "data/exp/iTrust/relation/CLASS_relationInfo_whole.ser";
    public static String vsmExpExportPath_ICSME = "data/exp/iTrust/icsme_result/vsm";
    public static String jsExpExportPath_ICSME = "data/exp/iTrust/icsme_result/js";
    public static String lsiExpExportPath_ICSME = "data/exp/iTrust/icsme_result/lsi";
    public static String projectName = "iTrust";
    public String getRtmClassPath() {
		return rtmClassPath;
	}

    public String getRtmMethodPath() {
		return rtmMethodPath;
	}
	public String getUcPath() {
		return ucPath;
	}
	
	public String getClassDirPath() {
		return classDirPath;
	}
	public String getMethodDirPath() {
		return methodDirPath;
	}
	public String getVsmExpExportPath() {
		return vsmExpExportPath;
	}
	public String getVsmExpExportPath_RE() {
		return vsmExpExportPath_RE;
	}
	public String getJsExpExportPath() {
		return jsExpExportPath;
	}
	public String getJsExpExportPath_RE() {
		return jsExpExportPath_RE;
	}
	public String getMethod_RelationInfoPath() {
		return method_relationInfoPath;
	}
	public String getClass_RelationInfoPath() {
		return class_relationInfoPath;
	}
	public String getClass_RelationInfoPathWhole() {
		return class_relationInfoPathWhole;
	}

	public String getVsmExpExportPath_ICSME() {
		return vsmExpExportPath_ICSME;
	}
	
	public String getJsExpExportPath_ICSME() {
		return jsExpExportPath_ICSME;
	}
	
	public String getLsiExpExportPath_ICSME() {
		return lsiExpExportPath_ICSME;
	}

	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getRtmClassPathNew() {
		return null;
	}

	@Override
	public String getClassDirPathNew() {
		// TODO Auto-generated method stub
		return null;
	}
}
