package cn.edu.nju.cs.itrace4.core.metrics;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.metrics.cut.*;

/**
 * Created by niejia on 15/2/24.
 */
public class MetricComputation {

    private SimilarityMatrix oracle;
    private SimilarityMatrix matrix;

    public MetricComputation(SimilarityMatrix matrix, SimilarityMatrix oracle) {
        this.matrix = matrix;
        this.oracle = oracle;
    }

    public Result compute(CutStrategy strategy, double argument) {
        if (strategy.equals(CutStrategy.CONSTANT_CUT)) return cmptConstantCut((int) argument);
        else if (strategy.equals(CutStrategy.VARIABLE_CUT)) return cmptVariableCut(argument);
        else if (strategy.equals(CutStrategy.CONSTANT_THRESHOLD)) return cmptConstantThreshold(argument);
        else if (strategy.equals(CutStrategy.VARIABLE_THRESHOLD)) return cmptVariableThreshold(argument);
        else return cmptScaleThreshold(argument);

    }

    public Result compute(CutStrategy strategy) {
        if (strategy.equals(CutStrategy.CONSTANT_CUT)) return cmptConstantCut(matrix.getCutN());
        else if (strategy.equals(CutStrategy.VARIABLE_CUT)) return cmptVariableCut(matrix.getVariableCut());
        else if (strategy.equals(CutStrategy.CONSTANT_THRESHOLD)) return cmptConstantThreshold(matrix.getThreshold());
        else if (strategy.equals(CutStrategy.VARIABLE_THRESHOLD)) return cmptVariableThreshold(matrix.getVariableThreshold());
        else return cmptScaleThreshold(matrix.getScaleThreshold());
    }

    private Result cmptConstantThreshold(double argument) {
        ConstantThresholdResult result = new ConstantThresholdResult(matrix, oracle);
        result.setCutParameter(argument);
        return result;
    }

    private Result cmptConstantCut(int argument) {
        ConstantCutResult result = new ConstantCutResult(matrix, oracle);
        result.setCutParameter(argument);
        return result;
    }

    private Result cmptVariableCut(double argument) {
        VariableCutResult result = new VariableCutResult(matrix, oracle);
        result.setCutParameter(argument);
        return result;
    }

    private Result cmptVariableThreshold(double argument) {
        VariableThresholdResult result = new VariableThresholdResult(matrix, oracle);
        result.setCutParameter(argument);
        return result;
    }

    private Result cmptScaleThreshold(double argument) {
        ScaleThresholdResult result = new ScaleThresholdResult(matrix, oracle);
        result.setCutParameter(argument);
        return result;
    }
}
