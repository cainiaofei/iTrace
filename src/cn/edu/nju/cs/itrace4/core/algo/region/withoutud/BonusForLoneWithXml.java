package cn.edu.nju.cs.itrace4.core.algo.region.withoutud;



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

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamic;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamicCount;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamicFirstDefaultValid;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.util.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
 
public class BonusForLoneWithXml{
	
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
    
	public BonusForLoneWithXml() throws ParserConfigurationException, SAXException, IOException{
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
		modelMap.put("jsd", "cn.edu.nju.cs.itrace4.core.ir.JSD");
		modelMap.put("lsi", "cn.edu.nju.cs.itrace4.core.ir.LSI");
	} 
	
	private void initProjectMap() {
		projectMap.put("itrust", new Itrust());
		projectMap.put("gantt", new Gantt());
		projectMap.put("jhotdraw", new JhotDraw());
		projectMap.put("maven", new Maven());
		projectMap.put("infinispan", new Infinispan());
	}


	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
        		project.getRtmClassPath());

        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        
        //LookForBug.getClassFromRI(ri);
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
       // ri.setPruning(0.4, 0.7);
        System.out.println("IR Count:"+getResultSize(result_ir));
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_CallDataDynamic = IR.compute(textDataset,model,
        		new UD_CallDataDynamic(ri,callEdgeScoreThreshold,
        				dataEdgeScoreThreshold,percent,valid));//0.7
        
        valid = new HashMap<String,Set<String>>();
        Result result_UD_CallDataDynamic1 = IR.compute(textDataset,model,
        		new UD_CallDataDynamicCount(ri,0.6,
        				0.7,1,valid));//0.7
        valid = new HashMap<String,Set<String>>();
        Result result_UD_CallDataDynamicFirstDefaultValid = IR.compute(textDataset,model,
        		new UD_CallDataDynamicFirstDefaultValid(ri,0.6,
        				0.7,0,valid));//0.7
        double callThreshold = 0.6, dataThreshold = 0.7;
        ri.setPruning(0.6, 0.7);
        Result result_ite_CallDataDynamic = IR.compute(textDataset,model,
        		new Ite_CallDataDynamic(ri,callThreshold,dataThreshold));//0.7
        
        
        MyVisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_ite_CallDataDynamic);
        curve.addLine(result_UD_CallDataDynamic1);
        curve.addLine(result_UD_CallDataDynamicFirstDefaultValid);
        //curve.addLine(result_pruningeCall_Data_Dir);
        //curve.addLine(result_UD_CallThenDataProcessLoneInnerMean07);//累加 内部 直接平均
        //curve.addLine(result_UD_DataThenCallProcessLoneInnerMean07);
//        double irPvalue = printPValue(result_ir, result_UD_CallDataDynamic);
//        double udPvalue = printPValue(result_UD_CSTI, result_UD_CallDataDynamic);
//        String irPvalueStr = (irPvalue+"").substring(0, 5);
//        String udPvalueStr = (udPvalue+"").substring(0, 5);
//        double rate = Double.valueOf(System.getProperty("rate"));
//        String rateStr = (rate+"").substring(0, 5);
//        
//        double udMap = result_UD_CSTI.getMeanAveragePrecisionByQuery();
//        double clusterMap = result_UD_CallDataDynamic.getMeanAveragePrecisionByQuery();
//        String udMapStr = (udMap+"").substring(0,4);
//        String clusterMapStr = (clusterMap+"").substring(0, 4);
        
//        curve.showChart(project.getProjectName()+"-"+udPvalueStr+"-rate:"+rateStr+"udMAP:"+udMapStr
//        		+"cluster:"+clusterMapStr);
//        curve.curveStore("./备份/infinispan数据收集_2017_11_05",project.getProjectName()+"-"+percent+"-"+callEdgeScoreThreshold+"-"+
//        		dataEdgeScoreThreshold+"-"+model+irPvalueStr+"-"+udPvalueStr);
  //      getApAndMap(result_ir,result_UD_CSTI, result_UD_CallDataDynamic);
          curve.showChart(project.getProjectName());
    }

	private void getApAndMap(Result result_ir,Result result_ud, Result result_cluster) {
		double ir_map = result_ir.getMeanAveragePrecisionByQuery();
		double ud_map = result_ud.getMeanAveragePrecisionByQuery();
		double cluster_map = result_cluster.getMeanAveragePrecisionByQuery();
		double pValue = printPValue(result_ud, result_cluster);
		System.out.println("ir:"+ir_map);
		System.out.println("ud:"+ud_map);
		System.out.println("CallExtendData:"+cluster_map);
		System.out.println("pValue:"+pValue);
		System.out.println("----------------------------");
//		Map<String,Double> udReqValue = result_ud.getAveragePrecisionByQuery();
//		Map<String,Double> clusterReqValue = result_cluster.getAveragePrecisionByQuery();
//		for(String req:udReqValue.keySet()) {
//			System.out.println("--------------------------");
//			System.out.println(req);
//			System.out.println("ud:"+udReqValue.get(req)+"----"+"cluster:"+clusterReqValue.get(req));
//		}
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
    	BonusForLoneWithXml bonusForLoneBoot = new BonusForLoneWithXml();
    	bonusForLoneBoot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }
/**
 0.145985401459854
ir:0.5680616767132588
ud:0.5935984042437915
mergeCallData:0.6007242381252754
pValue:0.6356062499299993
 */
}
