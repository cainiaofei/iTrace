package cn.edu.nju.cs.itrace4.exp.gantt.preprogress;

import java.io.IOException;
import java.util.HashSet;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.PruneBaseRI;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnion;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * Created by niejia on 15/3/17.
 */
public class PreprocessTextGantt {

    private static String rtmDBFilePath = "data/exp/Gantt/rtm/Gantt-req.db";
    private static String srcDirPath = "data/exp/Gantt/src";
    private static String ucDirPath = "data/exp/Gantt/uc";

    private static String classDirPath = "data/exp/Gantt/class/code";
    private static String methodDirPath = "data/exp/Gantt/method/code";

    private static String relationDirPath = "data/exp/Gantt/relation";

    public static void main(String[] args) throws IOException {
        SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();

        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        PruneBaseRI.pruneBaseRT("./data/exp/Gantt", new HashSet<String>(rg.getVertexIdNameMap().values()));
        rg.showMessage();
    }
}
