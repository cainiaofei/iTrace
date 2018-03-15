package cn.edu.nju.cs.itrace4.exp.maven.preprocess;

import java.io.File;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.GetSrc;
import cn.edu.nju.cs.itrace4.exp.tool.GetSrcBaseRI;
import cn.edu.nju.cs.itrace4.exp.tool.GetUC;
import cn.edu.nju.cs.itrace4.exp.tool.TransferTXT;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnionForGit;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTM;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTMExt;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTMExtMsg;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTMThroughCluster;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @author zzf
 * @date 2017/10/11
 * @descrition copy from <code>PreprocessTextjHotDraw</code> 
 */
public class PreprocessTextMaven {
	private GenerateRTM getRTM;
	//private GenerateRTMThroughCluster getRTM;
	private GetUC getUC = new GetUC();
	//private GetSrc getOriginSrc = new GetSrc(rtmDBFilePath);
	private GetSrc getOriginSrc = new GetSrcBaseRI(relationDirPath);
	
	private TransferTXT getSrc = new TransferTXT();
	//private TableFormatNormalize generateCallGraph = new TableFormatNormalize();
	
	private static String projectPath = "data/exp/Maven_TestCase/";

    private static String rtmDBFilePath = projectPath + "rtm/Maven-req.db";
    private static String srcDirPath = projectPath + "src";
    private static String ucDirPath = projectPath + "uc";

    private static String classDirPath = projectPath + "class/code";
    private static String methodDirPath = projectPath + "method/code";
    
    private static String relationDirPath = projectPath + "relation";
   
    private static String masterPath = projectPath + "maven-master";
    private static String graphDBPath = relationDirPath + File.separator + "call.db";
    
    private String dbProperty = "resource/mavenDB.property";
    private String sqlFile = "resource/sql/buildRTMForMaven.sql";
    
    public PreprocessTextMaven() {
    	//2017.11.27 change temporary 
    	//getRTM = new GenerateRTM(rtmDBFilePath,dbProperty,sqlFile);
    	getRTM = new GenerateRTMExt(rtmDBFilePath,dbProperty,sqlFile);
    	//getRTM = new GenerateRTMExtMsg(rtmDBFilePath,dbProperty,sqlFile);
    	//getRTM = new GenerateRTMThroughCluster(rtmDBFilePath,dbProperty,sqlFile);
    }
    
    private void cleanData() {
    	deleteFileInDir(srcDirPath);
    	deleteFileInDir(classDirPath);
    	deleteFileInDir(ucDirPath);
    }
    
    private void deleteFileInDir(String dirPath) {
    	File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for(File f:files) {
			f.delete();
		}
	}

	public void arrangeData() {
		getRTM.buildRTMTable();
    	try {
    		cleanData();
			getUC.getUCFromDB(ucDirPath,rtmDBFilePath);
			String originPath = srcDirPath;
	    	String targetPath = classDirPath;
	    	//getOriginSrc.getSrcFromMasterBasedOnGraphDB(masterPath, srcDirPath, graphDBPath);
//	    	getOriginSrc.getSrcFromMasterBasedOnGraphDBNewFormatDB(masterPath,originPath,graphDBPath,"callGraph",
//	    			"caller","callee");
	    	
	    	getOriginSrc.getSrcBaseRI(masterPath, originPath);
	    	getSrc.transferTXT(originPath, targetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	
	public static void main(String[] args) {
        /*
         * @author zzf <tiaozhanzhe668@163.com>
         * @date 2017/11/3
         * @description:
         *              step1:  get rtm from Maven-req
         *              step2:  get src from code-master
         *              step3:  get uc from rtm
         *              step4:  get relation info
         *              step5:  get union between class,uc,ri and rtm.
         *              step6:  process txt. 
         */
    	PreprocessTextMaven mavenProcess = new PreprocessTextMaven();
    	mavenProcess.arrangeData();
    	
    	SourceTargetUnionForGit union = new SourceTargetUnionForGit(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();
        
        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        rg.showMessage();
    }
	
//    public static void main(String[] args) {
//        /*
//         * @author zzf <tiaozhanzhe668@163.com>
//         * @date 2017/10/11
//         * @description:为了搞清楚<code>预处理</code>和 <code>序列化</code>的整体流程，先把无关的注释掉
//         */
//    	PreprocessTextMaven MavenProcess = new PreprocessTextMaven(rtmDBFilePath);
//    	MavenProcess.cleanData();
//    	MavenProcess.arrangeData();
//    	
//    	SourceTargetUnionForGit union = new SourceTargetUnionForGit(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);
//
//        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
//        preprocess.doProcess();
//        
//        System.setProperty("projectType", "git");
//        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
//        rg.showMessage();
//    }
}
