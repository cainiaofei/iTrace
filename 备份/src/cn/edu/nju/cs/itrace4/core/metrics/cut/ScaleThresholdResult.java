package cn.edu.nju.cs.itrace4.core.metrics.cut;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

/**
 * Created by niejia on 14-8-29.
 */
public class ScaleThresholdResult extends Result {

    public ScaleThresholdResult(SimilarityMatrix matrix, SimilarityMatrix oracle) {
        super(matrix, oracle);
        resultName = "scale threshold";
        cutParameter = originMatrix.getScaleThreshold();
    }

    @Override
    public void showMetrics() {
    }

    public void setScaleThreshold(double val) {
        setCutParameter(val);
    }

    @Override
    public void setCutParameter(double val) {

        SimilarityMatrix sims = new SimilarityMatrix();
        originMatrix.setScaleThreshold(val);
        cutParameter = val;
        for (SingleLink link : originMatrix.allLinks()) {
            if (originMatrix.isLinkAboveScaleThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            } else {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), 0.0);
            }
        }
        matrix = sims;
    }
}
