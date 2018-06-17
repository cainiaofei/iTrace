package cn.edu.nju.cs.itrace4.demo.explore;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.PruningCall_Data_CSTI;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Count_Percent;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.util.Setting;
import javafx.util.Pair;

import java.util.*;

/**
 * 
 */
public class Closeness_UD implements CSTI {

    private RelationGraph relationGraph;
    private RelationInfo relationInfo;
    private final UseEdge useEdge;

//    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private List<String> correctImprovedTargetsList;
    private double percent;
    private  PruningCall_Data_Connection_Closenss closeness;
    		
    
    
    public Closeness_UD(RelationInfo relationInfo,double percent,RelationInfo class_relationForO,
    		RelationInfo class_relationForAllDependencies) {
        this.relationInfo = relationInfo;
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.useEdge = UseEdge.Call_Data;
        log = new StringBuilder();
        this.percent = percent;
        correctImprovedTargetsList = new ArrayList<>();
        relationInfo.setPruning(Setting.callThreshold, Setting.dataThreshold);
        class_relationForO.setPruning(0.5, 0.5);
        class_relationForAllDependencies.setPruning(0.5, 0.5);
        closeness = new PruningCall_Data_Connection_Closenss(relationInfo, class_relationForO, 
				class_relationForAllDependencies,
				UseEdge.Call_Data, 1.0, 1.0);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, 
    		SimilarityMatrix similarityMatrix) {
    	UD_CSTI_First_Count_Percent ud_percent = new UD_CSTI_First_Count_Percent(relationInfo,percent);
    	SimilarityMatrix originMatrix = closeness.improve(similarityMatrix, textDataset,similarityMatrix);
    	LinksList allLinks = originMatrix.allLinks();
    	LinksList resultLinks = new LinksList();
    	
    	Collections.sort(allLinks, Collections.reverseOrder());
        for (SingleLink link : allLinks) {
        	resultLinks.add(link);
        }
    	
        SimilarityMatrix matrix_ud = new SimilarityMatrix();
        for (SingleLink link : resultLinks) {
             matrix_ud.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
         }
    	
    	return ud_percent.improve(matrix_ud, textDataset,matrix_ud);
        //return matrix_ud;
    }


	@Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }


    @Override
    public String getAlgorithmName() {
        return "Closeness_UD"+percent;
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p1 = new Pair<>("CallEdgeScoreThreshold", String.valueOf(relationInfo.getCallEdgeScoreThreshold()));
        Pair<String, String> p2 = new Pair<>("DataEdgeScoreThreshold", String.valueOf(relationInfo.getDataEdgeScoreThreshold()));
        parameters.add(p1);
        parameters.add(p2);
        return parameters;
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return correctImprovedTargetsList;
    }
}
