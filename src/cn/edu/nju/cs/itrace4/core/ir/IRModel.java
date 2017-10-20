package cn.edu.nju.cs.itrace4.core.ir;

import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.TermDocumentMatrix;

/**
 * Created by niejia on 15/2/23.
 */
public interface IRModel {
    public SimilarityMatrix Compute(ArtifactsCollection source, ArtifactsCollection target);

    public TermDocumentMatrix getTermDocumentMatrixOfQueries();

    public TermDocumentMatrix getTermDocumentMatrixOfDocuments();
}
