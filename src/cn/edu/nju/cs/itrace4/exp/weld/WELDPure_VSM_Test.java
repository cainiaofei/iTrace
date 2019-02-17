package cn.edu.nju.cs.itrace4.exp.weld;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

import java.io.IOException;

/**
 * Created by niejia on 16/3/11.
 */
public class WELDPure_VSM_Test {


    public static void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(WELD_CONSTANTS.ucPath, WELD_CONSTANTS.classDirPath, WELD_CONSTANTS.rtmClassPath);

        Result result_ir_vsm = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_ir_jsd = IR.compute(textDataset, IRModelConst.JSD, new None_CSTI());
        Result result_ir_lsi = IR.compute(textDataset, IRModelConst.LSI, new None_CSTI());

        System.out.println("ir_vsm");
        result_ir_vsm.showAveragePrecisionByRanklist();
        result_ir_vsm.showMeanAveragePrecisionByQuery();

        System.out.println("ir_jsd");
        result_ir_jsd.showAveragePrecisionByRanklist();
        result_ir_jsd.showMeanAveragePrecisionByQuery();

        System.out.println("ir_lsi");
        result_ir_lsi.showAveragePrecisionByRanklist();
        result_ir_lsi.showMeanAveragePrecisionByQuery();

        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir_vsm);
        curve.addLine(result_ir_jsd);
        curve.addLine(result_ir_lsi);
        curve.showChart();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        run();
    }

}
