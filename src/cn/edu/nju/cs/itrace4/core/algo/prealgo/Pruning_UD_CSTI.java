package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.List;

/**
 * Created by niejia on 15/3/17.
 */
public class Pruning_UD_CSTI implements CSTI {

    private CSTI ud_csti;
    private CSTI pruning_csti;

    public Pruning_UD_CSTI(RelationInfo relationInfo, UseEdge useEdge) {
        this.ud_csti = new UD_CSTI(relationInfo);
        this.pruning_csti = new PruningCall_Data_CSTI(relationInfo, useEdge);
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
        return "Pruning_UD_CSTI";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        return null;
    }

    @Override
    public String getDetails() {
        return null;
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }
}
