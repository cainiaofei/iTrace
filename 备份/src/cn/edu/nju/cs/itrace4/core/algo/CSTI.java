package cn.edu.nju.cs.itrace4.core.algo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import javafx.util.Pair;

import java.util.List;

/**
 * Created by niejia on 15/3/3.
 */
public interface CSTI {
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix);

    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset);

    public String getAlgorithmName();

    public List<Pair<String, String>> getAlgorithmParameters();

    public String getDetails();

    public List<String> getCorrectImprovedTargetsList();
}
