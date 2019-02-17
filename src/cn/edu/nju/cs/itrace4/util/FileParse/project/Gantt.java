package cn.edu.nju.cs.itrace4.util.FileParse.project;

public class Gantt implements Project{
	public String rtmClassPath = "data/exp/Gantt/rtm/RTM_CLASS.txt";
    public String rtmClassPathNew = "data/exp/Gantt/rtm/RTM_CLASS_New.txt";
    public String rtmMethodPath = "data/exp/Gantt/rtm/RTM_Method.txt";
    public String ucPath = "data/exp/Gantt/uc";
	public String classDirPath = "data/exp/Gantt/class/code";
    public String classDirPathNew = "data/exp/Gantt/class/code_new";
    public String methodDirPath = "data/exp/Gantt/method/code";
    public String vsmExpExportPath = "data/exp/Gantt/result/vsm";
    public String jsExpExportPath = "data/exp/Gantt/result/js";
    public String class_relationInfoPath = "data/exp/Gantt/relation/CLASS_relationInfo.ser";
    public String class_relationInfoPathWhole = "data/exp/Gantt/relation/CLASS_relationInfo_whole.ser";
    public String method_relationInfoPath = "data/exp/Gantt/relation/Method_relationInfo.ser";
    public String vsmExpExportPath_RE = "data/exp/Gantt/result/vsm_re";
    public String jsExpExportPath_RE = "data/exp/Gantt/result/js_re";
    public String vsmExpExportPath_ICSME = "data/exp/Gantt/icsme_result/vsm";
    public String jsExpExportPath_ICSME = "data/exp/Gantt/icsme_result/js";
    public String lsiExpExportPath_ICSME = "data/exp/Gantt/icsme_result/lsi";  
    public String projectName = "Gantt";
	
    @Override
    public String getRtmClassPath() {
		return rtmClassPath;
	}
	
    @Override
	public String getRtmClassPathNew() {
		return rtmClassPathNew;
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
	public String getClassDirPathNew() {
		return classDirPathNew;
	}
	
	@Override
	public String getMethodDirPath() {
		return methodDirPath;
	}
	
	@Override
	public String getVsmExpExportPath() {
		return vsmExpExportPath;
	}
	
	@Override
	public String getJsExpExportPath() {
		return jsExpExportPath;
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
	public String getMethod_RelationInfoPath() {
		return method_relationInfoPath;
	}
	
	@Override
	public String getVsmExpExportPath_RE() {
		return vsmExpExportPath_RE;
	}
	
	@Override
	public String getJsExpExportPath_RE() {
		return jsExpExportPath_RE;
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
}
