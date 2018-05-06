package cn.edu.nju.cs.itrace4.demo.algo.coderegion.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.algo.coderegion.UD_CodeTextAsWholeInRegion;
import cn.edu.nju.cs.itrace4.demo.algo.coderegion.UD_MergeCodeTXTAndNewRepresentElement;
import cn.edu.nju.cs.itrace4.demo.cdgraph.inneroutter.UD_InnerAndOuterSeq;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_Cluster_40;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.demo.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
 
public class BootWithNewWayRepresentClass{
	
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
    
	public BootWithNewWayRepresentClass() throws ParserConfigurationException, SAXException, IOException{
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
        
        int userVerifyCount = (int)(ri.getVertexIdNameMap().size()*percent);
        //LookForBug.getClassFromRI(ri);
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        System.out.println("IR Count:"+getResultSize(result_ir));
        
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        Result result_UD_sortByMergeCodeInRegion = IR.compute(textDataset, model,
				new UD_CodeTextAsWholeInRegion(project,ri, callEdgeScoreThreshold, dataEdgeScoreThreshold, 
						userVerifyCount,valid,model));// 0.7
        
        valid = new HashMap<String,Set<String>>();
        Result result_UD_mergeCodeReprentClassNewWay = IR.compute(textDataset, model,
				new UD_MergeCodeTXTAndNewRepresentElement(project,ri, callEdgeScoreThreshold, dataEdgeScoreThreshold, 
						userVerifyCount,valid,model));// 0.7
        
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        valid = new HashMap<String,Set<String>>();
        Result result_UD_sortByBiggestCodeInRegion = IR.compute(textDataset, model,
				new UD_InnerAndOuterSeq(ri, callEdgeScoreThreshold, dataEdgeScoreThreshold, 
						userVerifyCount,valid));// 0.7
        
        MyVisualCurve curve = new MyVisualCurve();
        
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        //curve.addLine(result_pruningeCall_Data_Dir);
        curve.addLine(result_UD_sortByMergeCodeInRegion);//累加 内部 直接平均
        curve.addLine(result_UD_sortByBiggestCodeInRegion);
        curve.addLine(result_UD_mergeCodeReprentClassNewWay);
        
        double irPvalue = printPValue(result_ir, result_UD_sortByMergeCodeInRegion);
        double udPvalue = printPValue(result_UD_CSTI, result_UD_sortByMergeCodeInRegion);
        String irPvalueStr = (irPvalue+"").substring(0, 5);
        String udPvalueStr = (udPvalue+"").substring(0, 5);
        double rate = Double.valueOf(System.getProperty("rate"));
        String rateStr = (rate+"").substring(0, 5);
        curve.showChart(project.getProjectName()+"-"+irPvalueStr+"-"+udPvalueStr+"-"+rateStr);
        curve.curveStore(".",project.getProjectName()+"-"+percent+"-"+callEdgeScoreThreshold+"-"+
        		dataEdgeScoreThreshold+"-"+model+irPvalueStr+"-"+udPvalueStr);
    }

	
	private int getResultSize(Result result) {
		int count = 0;
		for(String key:result.matrix.sourceArtifactsIds()) {
			count += result.matrix.getLinksForSourceId(key).size();
		}
		return count;
	}
	
	
	private void storeList(List<String> aPList, List<String> mAPList) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+
	File.separator+project.getProjectName()+File.separator+model+"_MAP.txt")));
		for(String str:aPList){
			bw.write(str);
			bw.newLine();
		}
		for(String str:mAPList){
			bw.write(str);
			bw.newLine();
		}
		bw.close();
	}

	private void compare(Result ours, Result compareTo) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+
				File.separator+project.getProjectName()+File.separator+model+"_compare.csv")));
        List<Double> oursPrecisionList = ours.getPrecisionAtRecallByTen();
        List<Double> compareToPrecisionList = compareTo.getPrecisionAtRecallByTen();
        List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen();
        List<Integer> compareFP = compareTo.getFalsePositiveAtRecallByTen();

        int recall = 10;

        for (int i = 0; i < oursPrecisionList.size(); i++) {
            double ourPrecision = oursPrecisionList.get(i);
            double theirPrecision = compareToPrecisionList.get(i);
            bw.write(ourPrecision-theirPrecision+"%;");
//            System.out.println("Recall " + recall);
//            System.out.println(ourPrecision - theirPrecision);
            int ourFP = oursFP.get(i);
            int theirFP = compareFP.get(i);
            bw.write((ourFP-theirFP)*1.0/ourFP*100+"%;");
            //System.out.println(ourFP - theirFP);
            recall += 10;
        }
        bw.close();
    }
	
	private void storePrecisionBaseOnRecall(Map<Integer, List<Double>> map) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+
				project.getProjectName()+File.separator+project.getProjectName()+"_"+model+".csv")));
		for(int i = 1; i <= 10;i++){
			bw.write(0.1*i+";");
			List<Double> precisionList = map.get(i);
			for(int j = 0; j < precisionList.size()-1;j++){
				bw.write(precisionList.get(j)+";");
			}
			bw.write(precisionList.get(precisionList.size()-1)+"");
			bw.newLine();
		}
		bw.close();
	}

	private void initMap(Map<Integer,List<Double>> map) {
		for(int i = 1; i <= 10;i++){
			map.put(i, new ArrayList<Double>());
		}
	}

	private void storeRFile(List<String> list) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+
				project.getProjectName()+"_"+System.currentTimeMillis()+model +".R")));
		for(String str:list){
			bw.write(str);
			bw.newLine();
		}
		bw.write("print(wilcox.test(innerMean,IR))\r\n" + 
				"print(wilcox.test(innerMean,UD))");
		bw.close();
	}

	private void expressWithExcel(Result result_ir, Result result_UD_CSTI, Result result_UD_CallThenDataIgnoreLone,
			Result result_UD_CallThenDataWithBonusForLone) throws IOException {
		Result[] results = {result_ir,result_UD_CSTI,result_UD_CallThenDataIgnoreLone,
				result_UD_CallThenDataWithBonusForLone};
		/*
		 * write the first line: modelName, algorithmName 
		 */
		BufferedWriter bw = new BufferedWriter(new FileWriter("out"+File.separator+
				project.getProjectName()+File.separator+
				result_ir.getModel()+".csv"));
		bw.write(result_ir.getModel()+";");
		for(Result result:results){
			bw.write(result.getAlgorithmName()+";");
		}
		bw.newLine();
		/*
		 * write the second line: averagePrecision, and the averagePrecision of algorithm 
		 */
		bw.write("averagePrecision"+";");
		for(Result result:results){
			bw.write(result.getAveragePrecisionByRanklist()+";");
		}
		bw.newLine();
		/*
		 * write follow line, the req and corresponding precision 
		 */
		Set<String> reqSet = results[0].getAveragePrecisionByQuery().keySet();
		for(String req:reqSet){
			bw.write(req+";");
			for(Result result:results){
				bw.write(result.getAveragePrecisionByQuery().get(req)+";");
			}
			bw.newLine();
		}
		bw.close();
	}

	public void showRate(RelationInfo ri,TextDataset textDataset ){
		List<SubGraph> callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		List<SubGraph> dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		Map<Integer,String> vertexIdNameMap = ri.getVertexIdNameMap();
		Set<String> set = textDataset.getTargetCollection().keySet();
		filterSubGraphsList(set,callSubGraphList,vertexIdNameMap);
		filterSubGraphsList(set,dataSubGraphList,vertexIdNameMap);
		int nodesWhichRepresent = getSubGraphsCountMoreThanOne(callSubGraphList) + 
				getSubGraphsCountMoreThanOne(dataSubGraphList);
		int allNodesAmount = allNodes(dataSubGraphList);
		int another = allNodes(callSubGraphList);
		
		if(another!=allNodesAmount){
			System.out.println("居然不相等 明显有问题");
		}
		
		//System.out.println("------------rate---------------"+(nodesWhichRepresent*1.0)/allNodesAmount);
	}
	
	public void filterSubGraphsList(Set<String> set,List<SubGraph> subGraphList,
			Map<Integer,String> vertexIdNameMap){
		for(SubGraph subGraph:subGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = subGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}
	
	
    private int allNodes(List<SubGraph> subGraphs) {
    	int count = 0;
    	for(SubGraph subGraph:subGraphs){
    		count += subGraph.getVertexList().size();
    	}
    	return count;
	}

	private int getSubGraphsCountMoreThanOne(List<SubGraph> subGraphs) {
    	int count = 0;
    	for(SubGraph subGraph:subGraphs){
    		if(subGraph.getVertexList().size()>1){
    			count++;
    		}
    	}
    	return count;
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
    	BootWithNewWayRepresentClass bonusForLoneBoot = new BootWithNewWayRepresentClass();
    	bonusForLoneBoot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}
