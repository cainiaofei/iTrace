package cn.edu.nju.cs.itrace4.exp.maven.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.GetSrc;
import cn.edu.nju.cs.itrace4.exp.tool.GetUC;
import cn.edu.nju.cs.itrace4.exp.tool.PruneBaseRI;
import cn.edu.nju.cs.itrace4.exp.tool.RTMProcess;
import cn.edu.nju.cs.itrace4.exp.tool.TransferTXT;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnion;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnionForGit;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @author zzf
 * @date 2017/10/11
 * @descrition copy from <code>PreprocessTextjHotDraw</code> 
 */
public class PreprocessTextMaven {
	private GetUC getUC = new GetUC();
	private GetSrc getOriginSrc = new GetSrc();
	private TransferTXT getSrc = new TransferTXT();
	//private TableFormatNormalize generateCallGraph = new TableFormatNormalize();
	private RTMProcess rtmProcess = new RTMProcess();
	
	
	private static String projectPath = "data/exp/Maven/";

    private static String rtmDBFilePath = projectPath + "rtm/Maven-req.db";
    private static String srcDirPath = projectPath + "src";
    private static String ucDirPath = projectPath + "uc";

    private static String classDirPath = projectPath + "class/code";
    private static String methodDirPath = projectPath + "method/code";
    
    private static String relationDirPath = projectPath + "relation";
    
    private static String masterPath = projectPath+"maven-master";
    private static String graphDBPath = relationDirPath + File.separator + "call.db";
    
    
    public PreprocessTextMaven(String rtmDBFilePath) {
    	try {
			//generateCallGraph.generateFormatTable(callDBPath);
			rtmProcess.generateFinalRTM(rtmDBFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void cleanData() {
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
    	try {
			getUC.getUCFromDB(ucDirPath,rtmDBFilePath);
			String originPath = srcDirPath;
	    	String targetPath = classDirPath;
	    	getOriginSrc.getSrcFromMasterBasedOnGraphDB(masterPath, srcDirPath, graphDBPath);
	    	getSrc.transferTXT(originPath, targetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) throws IOException {
        /*
         * @author zzf <tiaozhanzhe668@163.com>
         * @date 2017/10/11
         * @description:为了搞清楚<code>预处理</code>和 <code>序列化</code>的整体流程，先把无关的注释掉
         */
    	PreprocessTextMaven MavenProcess = new PreprocessTextMaven(rtmDBFilePath);
    	MavenProcess.cleanData();
    	MavenProcess.arrangeData();
    	
    	SourceTargetUnionForGit union = new SourceTargetUnionForGit(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();
        
        //System.setProperty("projectType", "git");
        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        PruneBaseRI.pruneBaseRT("./data/exp/Maven", new HashSet<String>(rg.getVertexIdNameMap().values()));
        rg.showMessage();
    }
	
//	public static void main(String[] args) {
//        SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);
//
//        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
//        preprocess.doProcess();
//
//        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
//        rg.showMessage();
//    }
}
