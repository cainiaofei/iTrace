package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/21.
 */
public class PruningCall_O_CSTI implements CSTI {

    private RelationInfo unbrokenInfo;
    private RelationInfo brokenInfo;

    public PruningCall_O_CSTI(RelationInfo unbrokenInfo,RelationInfo brokenInfo) {
        this.unbrokenInfo = unbrokenInfo;
        this.brokenInfo = brokenInfo;
    }


    @Override

    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
//        SimilarityMatrix sm = (new O_CSTI(unbrokenInfo)).improve(matrix, textDataset);

        SimilarityMatrix sm = (new PruningCall_CSTI(brokenInfo, UseEdge.Call)).improve(matrix, textDataset, similarityMatrix);

        return (new O_CSTI(unbrokenInfo)).improve(sm, textDataset, similarityMatrix);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "Call_O";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("None", "");
        parameters.add(p);
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
