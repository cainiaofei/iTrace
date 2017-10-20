package cn.edu.nju.cs.itrace4.exp.etour;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

import java.io.IOException;

/**
 * Created by niejia on 15/12/18.
 */
public class ETourIR {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        TextDataset textDataset = new TextDataset(ETOUR_CONSTANTS.ucPath, ETOUR_CONSTANTS.classDirPath, ETOUR_CONSTANTS.rtmClassPath);
        TextDataset textDataset = new TextDataset(ETOUR_CONSTANTS.ucPath, ETOUR_CONSTANTS.classDirPath, ETOUR_CONSTANTS.rtmClassPath);

//        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
            Result result_ir_all = IR.compute(textDataset, IRModelConst.JSD, new None_CSTI());
//        Result result_ir_o = IR.compute(textDataset, IRModelConst.VSM_ALL, new O_CSTI());
//        Result result_ir_ud = IR.compute(textDataset, IRModelConst.VSM_ALL, new None_CSTI());
//        Result result_ir_vsm_both = IR.compute(textDataset, IRModelConst.VSM_Both, new None_CSTI());


//        result_ir.showAveragePrecisionByRanklist();
//        result_ir.showMeanAveragePrecisionByQuery();
//        result_ir_vsm_both.showMatrix();

            result_ir_all.showAveragePrecisionByRanklist();
            result_ir_all.showMeanAveragePrecisionByQuery();

//        result_ir_vsm_both.showAveragePrecisionByRanklist();
//        result_ir_vsm_both.showMeanAveragePrecisionByQuery();

        VisualCurve curve = new VisualCurve();
//        curve.addLine(result_ir);
            curve.addLine(result_ir_all);
//        curve.addLine(result_ir_vsm_both);
        curve.showChart();

//        String expName = "IR_Only";
//        curve.resultStore(ITRUST_CONSTANTS.jsExpExportPath, expName);
    }
}
