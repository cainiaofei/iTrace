package cn.edu.nju.cs.itrace4.core.metrics.cut;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

import java.io.Serializable;

/**
 * Created by niejia on 14-8-29.
 */
public class ConstantThresholdResult extends Result implements Serializable {

    public ConstantThresholdResult() {
        super();
    }

    public ConstantThresholdResult(SimilarityMatrix matrix, SimilarityMatrix oracle) {
        super(matrix, oracle);
        resultName = "constant threshold";
        cutParameter = originMatrix.getThreshold();
    }

    @Override
    public void showMetrics() {
    }

    public void setThreshold(double val) {
        setCutParameter(val);
    }

    @Override
    public void setCutParameter(double val) {

        SimilarityMatrix sims = new SimilarityMatrix();
        originMatrix.setThreshold(val);
        cutParameter = val;

        for (SingleLink link : originMatrix.allLinks()) {
            if (originMatrix.isLinkAboveThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            } else {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), 0.0);
            }
        }
        matrix = sims;
    }
}
