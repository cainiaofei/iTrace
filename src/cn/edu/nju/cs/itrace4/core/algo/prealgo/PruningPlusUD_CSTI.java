package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/3/19.
 */
public class PruningPlusUD_CSTI implements CSTI {

    private CSTI ud_csti;
    private CSTI pruning_csti;
    private RelationInfo relationInfo;

    public PruningPlusUD_CSTI(RelationInfo relationInfo, RelationInfo relationInfoForUD, UseEdge useEdge) {
        this.ud_csti = new UD_CSTI(relationInfoForUD);
        this.pruning_csti = new PruningCall_Data_CSTI(relationInfo, useEdge);
        this.relationInfo = relationInfo;
    }
    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        return ud_csti.improve(pruning_csti.improve(matrix, textDataset), textDataset);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "Pruning_UD";
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
        return null;
    }
}
