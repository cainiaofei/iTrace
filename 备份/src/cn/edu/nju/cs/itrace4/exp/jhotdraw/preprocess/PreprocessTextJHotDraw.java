package cn.edu.nju.cs.itrace4.exp.jhotdraw.preprocess;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.parser.SourceTargetUnion;
import cn.edu.nju.cs.itrace4.preprocess.BatchingPreprocess;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * Created by niejia on 16/3/12.
 */
public class PreprocessTextJHotDraw {

    private static String projectPath = "data/exp/JHotDraw/";

    private static String rtmDBFilePath = projectPath + "rtm/JHotDraw-req.db";
    private static String srcDirPath = projectPath + "src";
    private static String ucDirPath = projectPath + "uc";

    private static String classDirPath = projectPath + "class/code";
    private static String methodDirPath = projectPath + "method/code";
    private static String relationDirPath = "data/exp/JHotDraw/relation";

    public static void main(String[] args) {
        /*
         * @author zzf <tiaozhanzhe668@163.com>
         * @date 2017/10/11
         * @description:为了搞清楚<code>预处理</code>和 <code>序列化</code>的整体流程，先把无关的注释掉
         */
    	SourceTargetUnion union = new SourceTargetUnion(ucDirPath, srcDirPath, rtmDBFilePath, Granularity.CLASS,classDirPath,methodDirPath);

        BatchingPreprocess preprocess = new BatchingPreprocess(ucDirPath, classDirPath, methodDirPath);
        preprocess.doProcess();

        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);
        rg.showMessage();
    }
}
