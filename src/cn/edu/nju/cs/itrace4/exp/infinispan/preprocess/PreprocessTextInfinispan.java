package cn.edu.nju.cs.itrace4.exp.infinispan.preprocess;

import java.io.File;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.GetSrc;
import cn.edu.nju.cs.itrace4.exp.tool.GetUC;
import cn.edu.nju.cs.itrace4.exp.tool.TransferTXT;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnionForGit;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTM;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTMExt;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.GenerateRTMThroughCluster;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @author zzf
 * @date 2017/10/11
 * @descrition copy from <code>PreprocessTextjHotDraw</code> 
 */
public class PreprocessTextInfinispan {
	private GenerateRTM getRTM;
	private GetUC getUC = new GetUC();
	private GetSrc getOriginSrc = new GetSrc();
	private TransferTXT getSrc = new TransferTXT();
	//private TableFormatNormalize generateCallGraph = new TableFormatNormalize();
	
	private String clusterFilePath = "data/exp/Infinispan/clusterFile/s_d_t_70d.txt";
	private static String projectPath = "data/exp/Infinispan/";

    private static String rtmDBFilePath = projectPath + "rtm/Infinispan-req.db";
    private static String srcDirPath = projectPath + "src";
    private static String ucDirPath = projectPath + "uc";

    private static String classDirPath = projectPath + "class/code";
    private static String methodDirPath = projectPath + "method/code";
    
    private static String relationDirPath = projectPath + "relation";
    
    private static String masterPath = projectPath+"infinispan-master";
    private static String graphDBPath = relationDirPath + File.separator + "call.db";
    
    private String dbProperty = "resource/infinispanDB.property";
    private String sqlFile = "resource/sql/buildRTMForInfinispan.sql";
    
    
    public PreprocessTextInfinispan() {
    	//getRTM = new GenerateRTMExt(rtmDBFilePath, dbProperty, sqlFile);
    	//getRTM = new GenerateRTM(rtmDBFilePath, dbProperty, sqlFile);
    	getRTM = new GenerateRTMThroughCluster(rtmDBFilePath,dbProperty,sqlFile,clusterFilePath);
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
	    	
//	    	getOriginSrc.getSrcFromMasterBasedOnGraphDBNewFormatDB(masterPath,originPath,graphDBPath,
//	    			"callGraph","caller","callee");
	    	getOriginSrc.getSrcFromMasterBasedOnGraphDB(masterPath, srcDirPath, graphDBPath);
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
         *              step1:  get rtm from Infinispan-req
         *              step2:  get src from code-master
         *              step3:  get uc from rtm
         *              step4:  get relation info
         *              step5:  get union between class,uc,ri and rtm.
         *              step6:  process txt. 
         */
    	PreprocessTextInfinispan infinispanProcess = new PreprocessTextInfinispan();
    	infinispanProcess.arrangeData();
    	
    	SourceTargetUnionForGit union = new SourceTargetUnionForGit(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();
        
        //System.setProperty("projectType", "git");
        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        rg.showMessage();
    }
	
//    public static void main(String[] args) {
//        /*
//         * @author zzf <tiaozhanzhe668@163.com>
//         * @date 2017/10/11
//         * @description:为了搞清楚<code>预处理</code>和 <code>序列化</code>的整体流程，先把无关的注释掉
//         */
//    	PreprocessTextInfinispan infinispanProcess = new PreprocessTextInfinispan(rtmDBFilePath);
//    	infinispanProcess.cleanData();
//    	infinispanProcess.arrangeData();
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
