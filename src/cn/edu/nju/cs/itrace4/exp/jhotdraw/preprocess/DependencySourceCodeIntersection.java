package cn.edu.nju.cs.itrace4.exp.jhotdraw.preprocess;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.parser.DependencySourceCodeUnion;

/**
 * Created by niejia on 16/3/27.
 */
public class DependencySourceCodeIntersection {

    private static String projectPath = "data/exp/JHotDraw/";

    private static String rtmDBFilePath = projectPath + "rtm/JHotDraw-req.db";
    private static String srcDirPath = projectPath + "src";
    private static String ucDirPath = projectPath + "uc";

    private static String classDirPath = projectPath + "class/code";
    private static String methodDirPath = projectPath + "method/code";

    private static String relationDirPath = "data/exp/JHotDraw/relation";

    public static void main(String[] args) {
        DependencySourceCodeUnion union = new DependencySourceCodeUnion(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS, classDirPath, methodDirPath, relationDirPath);

        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        rg.showMessage();
    }
}
