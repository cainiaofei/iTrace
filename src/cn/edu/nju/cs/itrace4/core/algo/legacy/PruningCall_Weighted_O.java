package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.PruningCall_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/26.
 */
public class PruningCall_Weighted_O implements CSTI {

    private RelationInfo infoForRest;
    private RelationInfo brokenInfo;

    public PruningCall_Weighted_O(RelationInfo infoForRest,RelationInfo brokenInfo) {
        this.infoForRest = infoForRest;
        this.brokenInfo = brokenInfo;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix sm = (new PruningCall_CSTI(brokenInfo, UseEdge.Call)).improve(matrix, textDataset, similarityMatrix);
        return (new O_Weighted_Call_CSTI(infoForRest)).improve(sm, textDataset, similarityMatrix);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningCall_Weighted_O";
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
