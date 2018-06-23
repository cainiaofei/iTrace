package cn.edu.nju.cs.itrace4.exp.weld;

import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.util.parser.SourceTargetUnion;

/**
 * Created by niejia on 16/3/11.
 */
public class PreprocessTextWeld {

    private static String rtmDBFilePath = "data/exp/weld/rtm/RTM_CLASS.txt";
    private static String srcDirPath = "data/exp/weld/src";
    private static String ucDirPath = "data/exp/weld/uc";

    private static String classDirPath = "data/exp/weld/class/code";
    private static String methodDirPath = "data/exp/weld/method/code";

    public static void main(String[] args) {
        SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath, rtmDBFilePath, classDirPath, methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();
    }

}
