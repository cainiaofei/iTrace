package cn.edu.nju.cs.itrace4.exp.aqualush;

import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;

import java.io.File;

/**
 * Created by niejia on 15/6/25.
 */
public class PreprocessTextAqualush {

    private static String srcDirPath = "data/exp/Aqualush/src";
    private static String ucDirPath = "data/exp/Aqualush/uc";

    private static String classDirPath = "data/exp/Aqualush/class/code";
    private static String methodDirPath = "data/exp/Aqualush/method/code";

    public static void main(String[] args) {

//        SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath,classDirPath, methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess();
        preprocess.preprocessUCFiles(new File(srcDirPath));
        preprocess.preprocessUCFiles(new File(ucDirPath));

//        preprocess.doProcess();
    }
}
