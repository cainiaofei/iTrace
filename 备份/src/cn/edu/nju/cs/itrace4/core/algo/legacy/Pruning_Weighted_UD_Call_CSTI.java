package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/24.
 */
public class Pruning_Weighted_UD_Call_CSTI implements CSTI {

    private CSTI ud_csti;
    private CSTI pruning_csti;

    public Pruning_Weighted_UD_Call_CSTI(RelationInfo fullRelationInfo, RelationInfo infoForRest, RelationInfo prunedRelationInfo) {
        pruning_csti = new PruningCall_PruningO(infoForRest, prunedRelationInfo);
        ud_csti = new UD_Weight_Call_CSTI(fullRelationInfo);
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
        return "Pruning_Weighted_UD_Call";
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
        return null;
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }
}
