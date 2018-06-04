package cn.edu.nju.cs.itrace4.core.algo.region;

import java.util.List;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import javafx.util.Pair;

public abstract class AlgoBaseOnCodeRegion implements CSTI{
	public abstract SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix);

	public abstract SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset);

	public abstract String getAlgorithmName();

	public abstract List<Pair<String, String>> getAlgorithmParameters();

	public abstract String getDetails();

	public abstract List<String> getCorrectImprovedTargetsList();
}
