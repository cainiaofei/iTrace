package cn.edu.nju.cs.itrace4.core.dataset;

import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.util.io.ArtifactsReader;
import cn.edu.nju.cs.itrace4.util.io.RTMIO;

/**
 * Created by niejia on 15/2/23.
 */
public class TextDataset {

    private ArtifactsCollection sourceCollection;
    private ArtifactsCollection targetCollection;
    private SimilarityMatrix rtm;

    public TextDataset(String sourceDirPath, String targetDirPath, String rtmPath) {
        this.setSourceCollection(ArtifactsReader.getCollections(sourceDirPath, ".txt"));
        this.setTargetCollection(ArtifactsReader.getCollections(targetDirPath, ".txt"));
        this.setRtm(RTMIO.createSimilarityMatrix(rtmPath));
    }

    public ArtifactsCollection getSourceCollection() {
        return sourceCollection;
    }

    public void setSourceCollection(ArtifactsCollection sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

    public ArtifactsCollection getTargetCollection() {
        return targetCollection;
    }

    public void setTargetCollection(ArtifactsCollection targetCollection) {
        this.targetCollection = targetCollection;
    }

    public SimilarityMatrix getRtm() {
        return rtm;
    }

    public void setRtm(SimilarityMatrix rtm) {
        this.rtm = rtm;
    }
}
