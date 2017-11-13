package cn.edu.nju.cs.itrace4.exp.itrust.preprocess;

import java.io.IOException;
import java.util.HashSet;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.PruneBaseRI;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnion;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * Created by niejia on 15/2/22.
 */
public class PreprocessTextITrust {

    private static String rtmDBFilePath = "data/exp/iTrust/rtm/iTrust-req.db";
    private static String srcDirPath = "data/exp/iTrust/src";
    private static String ucDirPath = "data/exp/iTrust/uc";

    private static String classDirPath = "data/exp/iTrust/class/code";
    private static String methodDirPath = "data/exp/iTrust/method/code";

    private static String relationDirPath = "data/exp/iTrust/relation";

    public static void main(String[] args) throws IOException {
        SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();

        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        PruneBaseRI.pruneBaseRT("./data/exp/iTrust", new HashSet<String>(rg.getVertexIdNameMap().values()));
        rg.showMessage();
    }
}
