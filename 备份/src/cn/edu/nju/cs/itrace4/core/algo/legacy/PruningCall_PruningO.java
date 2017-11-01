package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.*;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/21.
 */

/*
处理剩余点时，考虑了方向性
 */
public class PruningCall_PruningO implements CSTI {

    private RelationInfo infoForRest;
    private RelationInfo brokenInfo;

    public PruningCall_PruningO(RelationInfo infoForRest,RelationInfo brokenInfo) {
        this.infoForRest = infoForRest;
        this.brokenInfo = brokenInfo;
    }

    @Override

    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix sm = (new PruningCall_CSTI(brokenInfo, UseEdge.Call)).improve(matrix, textDataset, similarityMatrix);
        return (new O_Dir_CSTI(infoForRest)).improve(sm, textDataset, similarityMatrix);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningCall_PruningO";
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
