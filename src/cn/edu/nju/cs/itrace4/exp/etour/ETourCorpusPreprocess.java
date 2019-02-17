package cn.edu.nju.cs.itrace4.exp.etour;

import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.util.parser.BatchingParser;

/**
 * Created by niejia on 15/12/20.
 */
public class ETourCorpusPreprocess {
    private static String ucDirPath = "data/exp/eTour/uc";

    private static String classDirPath = "data/exp/eTour/class/code";
    private static String methodDirPath = "data/exp/eTour/method";



    public static void main(String[] args) {

        String srcDir = "data/exp/eTour/src";
        BatchingParser bp = new BatchingParser(srcDir);
        bp.parse();
//
        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();
    }
}
