package cn.edu.nju.cs.itrace4.core.algo.region.practice;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_Cluster_40;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
 
public class WhoCanGetMore{
	// user will stop verifying When the user continuously encounters a certain number of incorrect connections.
	private int wrongLinkThreshold = 16; 
	//private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	private AnalyzeResult analyzeResult;
	private Project project;
	private String model;
	private Map<String,Project> projectMap = new HashMap<String,Project>();
	private Map<String,String> modelMap = new HashMap<String,String>();
	private XmlParse xmlParse; 
	private double callEdgeScoreThreshold;
    private double dataEdgeScoreThreshold;
	private double percent;
    
	public WhoCanGetMore() throws ParserConfigurationException, SAXException, IOException{
		initProjectMap();
		initModelMap();
		analyzeResult = new AnalyzeResult();
		xmlParse = new XmlParse();
		//read xml
		String[] res = xmlParse.process();
		project = projectMap.get(res[0]);
		model = modelMap.get(res[1]);
		callEdgeScoreThreshold = Double.valueOf(res[2]);
		dataEdgeScoreThreshold = Double.valueOf(res[3]);
		percent = Double.valueOf(res[4]);
		System.setProperty("routerLen", Integer.valueOf(res[5])+"");
	}
	
	private void initModelMap(){
		modelMap.put("vsm","cn.edu.nju.cs.itrace4.core.ir.VSM");
		modelMap.put("js", "cn.edu.nju.cs.itrace4.core.ir.JSD");
		modelMap.put("lsi", "cn.edu.nju.cs.itrace4.core.ir.LSI");
	} 
	
	private void initProjectMap() {
		projectMap.put("itrust", new Itrust());
		projectMap.put("gantt", new Gantt());
		projectMap.put("jhotdraw", new JhotDraw());
		projectMap.put("maven", new Maven());
		projectMap.put("infinispan", new Infinispan());
		projectMap.put("maven_cluster_40", new Maven_Cluster_40());
	}


	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
        		project.getRtmClassPath());

        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        
//        Result result_UD_sortByMergeCodeInRegion = IR.compute(textDataset, model,
//				new UDAndCluster(ri, callEdgeScoreThreshold, dataEdgeScoreThreshold));// 0.7
        
        //UDCodeRegion
        Result result_UD_sortByMergeCodeInRegion = IR.compute(textDataset, model,
				new UDCodeRegion(ri, callEdgeScoreThreshold, dataEdgeScoreThreshold));// 0.7
        
        compareWhichGetMore(result_UD_sortByMergeCodeInRegion,result_UD_CSTI);
        validate(result_UD_sortByMergeCodeInRegion,result_UD_CSTI);
        
        
        MyVisualCurve curve = new MyVisualCurve();
        
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        //curve.addLine(result_pruningeCall_Data_Dir);
        curve.addLine(result_UD_sortByMergeCodeInRegion);//累加 内部 直接平均
        curve.showChart(project.getProjectName());;
        double irPvalue = printPValue(result_ir, result_UD_sortByMergeCodeInRegion);
        double udPvalue = printPValue(result_UD_CSTI, result_UD_sortByMergeCodeInRegion);
        String irPvalueStr = (irPvalue+"").substring(0, 5);
        String udPvalueStr = (udPvalue+"").substring(0, 5);
        curve.curveStore(".",project.getProjectName()+"-"+percent+"-"+callEdgeScoreThreshold+"-"+
        		dataEdgeScoreThreshold+"-"+model+irPvalueStr+"-"+udPvalueStr);
    }

	
	private void compareWhichGetMore(Result cluster, Result ud) {
		int correctCountThroughCluster = getCorrectCount(cluster);
		int correctCountThroughUD = getCorrectCount(ud);
		System.out.println("correctCountThroughCluster"+correctCountThroughCluster);
		System.out.println("correctCountThroughUD"+correctCountThroughUD);
	}
    
	private void validate(Result cluster, Result ud) {
		int correctCountThroughCluster = getCorrectCountWholeLinks(cluster);
		int correctCountThroughUD = getCorrectCountWholeLinks(ud);
		System.out.println("correctCountThroughCluster"+correctCountThroughCluster);
		System.out.println("correctCountThroughUD"+correctCountThroughUD);
	}
	
	
	
	private int getCorrectCountWholeLinks(Result result) { 
		int number = 1;
		int correctLinksCount = 0;
		SimilarityMatrix oracle = result.getOracle();
		LinksList allLinks = result.matrix.allLinks();
		Collections.sort(allLinks,Collections.reverseOrder());
		for(SingleLink link:allLinks) {
			if(oracle.isLinkAboveThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
				correctLinksCount++;
			}
			number++;
			if(number>2000) {
				return correctLinksCount;
			}
		}
		return correctLinksCount;
	}

	/**
	 * @author zzf
	 * @date 2018.5.17
	 * @description if current link is valid then set continueWrongCount zero, else increase continueWrongCount.
	 *  and compare whether continueWrongCount is more than wrongLinkThreshold.
	 */
	private int getCorrectCount(Result result) {
		int correctLinksCount = 0;
		SimilarityMatrix oracle = result.getOracle();
		LinksList allLinks = result.matrix.allLinks();
		Collections.sort(allLinks,Collections.reverseOrder());
		int continueWrongCount = 0;
		for(String req:result.matrix.sourceArtifactsIds()) {
			for(SingleLink link:allLinks) {
				if(!link.getSourceArtifactId().equals(req)) {
					continue;
				}
				else {
					if(oracle.isLinkAboveThreshold(req, link.getTargetArtifactId())) {
						continueWrongCount = 0;
						correctLinksCount++;
					}
					else {
						continueWrongCount++;
						if(continueWrongCount>=wrongLinkThreshold) {
							break;
						}
					}
				}
			}
		}
		return correctLinksCount;
	}

	private static double printPValue(Result ours, Result compareTo) {
	      MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
	      double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
	      double pValue_fp = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fp(), compareTo.getWilcoxonDataArray_fp());
//	      System.out.println("F-measure pValue = " + pValue_fmeasure );
//	      System.out.println("FP pValue = " + pValue_fp );
	      return pValue_fmeasure;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException,
			ParserConfigurationException, SAXException {
		long startTime = System.currentTimeMillis();
    	WhoCanGetMore bonusForLoneBoot = new WhoCanGetMore();
    	bonusForLoneBoot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}

//valid = new HashMap<String,Set<String>>();
//Result result_UD_mergeCodeReprentClassNewWay = IR.compute(textDataset, model,
//		new UD_MergeCodeTXTAndNewRepresentElement(project,ri, callEdgeScoreThreshold, dataEdgeScoreThreshold, 
//				userVerifyCount,valid,model));// 0.7
//
//ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
//valid = new HashMap<String,Set<String>>();
//Result result_UD_sortByBiggestCodeInRegion = IR.compute(textDataset, model,
//		new UD_InnerAndOuterSeq(ri, callEdgeScoreThreshold, dataEdgeScoreThreshold, 
//				userVerifyCount,valid));// 0.7
