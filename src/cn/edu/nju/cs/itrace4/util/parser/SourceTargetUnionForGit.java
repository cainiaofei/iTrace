package cn.edu.nju.cs.itrace4.util.parser;


import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.exp.tool.RemoveUCNotInRtmClass;
import cn.edu.nju.cs.itrace4.util.io.ArtifactsReader;
import cn.edu.nju.cs.itrace4.util.io.RTMIO;
import cn.edu.nju.cs.itrace4.util.io.SqliteIOForGit;
import cn.edu.nju.cs.itrace4.util.io._;
import cn.edu.nju.cs.itrace4.util.parser.BatchingParser;

import java.io.File;
import java.util.Set;

/**
 * @author zzf <tiaozhanzhe668@163.com>
 * @date 2017/10/12
 * @description make some change based on niejia code, because the rtm format of maven is different
 * 	with jHotDraw/iTrust/Gantt project.
 */
public class SourceTargetUnionForGit implements SourceTargetUnionInterface{
	private RemoveUCNotInRtmClass removeUC = new RemoveUCNotInRtmClass();

    public SourceTargetUnionForGit(String ucDirPath, String srcDirPath, String rtmDBFilePath, Granularity granularity, 
    		String classDirPath, String methodDirPath) {
    	SqliteIOForGit.setUCPath(ucDirPath);
        SimilarityMatrix rtm_class = SqliteIOForGit.readRTMFromDB(rtmDBFilePath, Granularity.CLASS);
        //SimilarityMatrix rtm_method = SqliteIO.readRTMFromDB(rtmDBFilePath, Granularity.METHOD);

        int rtmUCNum = rtm_class.sourceArtifactsIds().size();
        int rtmClassNum = rtm_class.targetArtifactsIds().size();
        //int rtmMethodNum = rtm_method.targetArtifactsIds().size();

        System.out.printf("RTM contains %d uc case.\n", rtmUCNum);
        System.out.printf("RTM contains %d classes.\n", rtmClassNum);
        //System.out.printf("RTM contains %d methods.\n", rtmMethodNum);

        BatchingParser bp = new BatchingParser(srcDirPath);
        bp.parse();

        // Delete Requirements and code which are not mentioned in RTM,

        deleteFilesNotInRTM(ucDirPath, rtm_class.sourceArtifactsIds(), "uc case");
        deleteFilesNotInRTM(classDirPath, rtm_class.targetArtifactsIds(), "class");
        //deleteFilesNotInRTM(methodDirPath, rtm_method.targetArtifactsIds(), "method");

        // Delete trace links in rtm that source/target artifact is not in src/uc.
        File rtmDB = new File(rtmDBFilePath);
        String rtmClassPath = rtmDB.getParent() + "/RTM_CLASS.txt";
       // String rtmMethodPath = rtmDB.getParent() + "/RTM_METHOD.txt";

        ArtifactsCollection ucCollection = ArtifactsReader.getCollections(ucDirPath, ".txt");
        ArtifactsCollection classCollection = ArtifactsReader.getCollections(classDirPath, ".txt");
       // ArtifactsCollection methodCollection = ArtifactsReader.getCollections(methodDirPath, ".txt");

        deleteTraceLinkNotFound(rtmClassPath, ucCollection.keySet(), classCollection.keySet(), Granularity.CLASS);
        deleteFilesNotInRTM(ucDirPath, rtm_class.sourceArtifactsIds(), "uc case");
        try {
			removeUC.removeUCNotInRtmClass(ucDirPath, rtmClassPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
        // deleteTraceLinkNotFound(rtmMethodPath, ucCollection.keySet(), methodCollection.keySet(), Granularity.METHOD);
    }

    public SourceTargetUnionForGit(String ucDirPath, String srcDirPath, String rtmDBFilePath, String classDirPath, String methodDirPath) {
//        SimilarityMatrix rtm_class = SqliteIO.readRTMFromDB(rtmDBFilePath, Granularity.CLASS);
        SimilarityMatrix rtm_class = RTMIO.createSimilarityMatrix(rtmDBFilePath);
        int rtmUCNum = rtm_class.sourceArtifactsIds().size();
        int rtmClassNum = rtm_class.targetArtifactsIds().size();

        System.out.printf("RTM contains %d uc case.\n", rtmUCNum);
        System.out.printf("RTM contains %d classes.\n", rtmClassNum);

        BatchingParser bp = new BatchingParser(srcDirPath);
        bp.parse();

        // Delete Requirements and code which are not mentioned in RTM,

        deleteFilesNotInRTM(ucDirPath, rtm_class.sourceArtifactsIds(), "uc case");
        deleteFilesNotInRTM(classDirPath, rtm_class.targetArtifactsIds(), "class");

        // Delete trace links in rtm that source/target artifact is not in src/uc.
        File rtmDB = new File(rtmDBFilePath);
        String rtmClassPath = rtmDB.getParent() + "/RTM_CLASS.txt";

        ArtifactsCollection ucCollection = ArtifactsReader.getCollections(ucDirPath, ".txt");
        ArtifactsCollection classCollection = ArtifactsReader.getCollections(classDirPath, ".txt");
        ArtifactsCollection methodCollection = ArtifactsReader.getCollections(methodDirPath, ".txt");

        deleteTraceLinkNotFound(rtmClassPath, ucCollection.keySet(), classCollection.keySet(), Granularity.CLASS);
    }


    public SourceTargetUnionForGit(String ucDirPath, String srcDirPath, String classDirPath, String methodDirPath) {

        BatchingParser bp = new BatchingParser(srcDirPath);
        bp.parse();

//        ArtifactsCollection ucCollection = ArtifactsReader.getCollections(ucDirPath, ".txt");
//        ArtifactsCollection classCollection = ArtifactsReader.getCollections(classDirPath, ".txt");
//        ArtifactsCollection methodCollection = ArtifactsReader.getCollections(methodDirPath, ".txt");

    }

    private void deleteTraceLinkNotFound(String rtmClassPath, Set<String> ucSet, Set<String> classSet, 
    		Granularity granularity) {
        String input = _.readFile(rtmClassPath);
        String[] lines = input.split("\n");

        StringBuilder sb = new StringBuilder();

        int deleteLinksNum = 0;
        int allLinksNum = 0;
        for (String line : lines) {
            allLinksNum++;
            String[] tokens = line.split(" ");
            String source = tokens[0];
            String target = tokens[1];

//            System.out.println(" line = " + line );

            if (ucSet.contains(source) && classSet.contains(target)) {
                sb.append(line);
                sb.append("\n");
            } else {
                deleteLinksNum++;
               // System.out.println("Delete trace link " + line);
            }
        }

        System.out.printf("Delete %d of %d trace links in %s RTM.\n", deleteLinksNum, allLinksNum, granularity);
        _.writeFile(sb.toString(), rtmClassPath);
    }

    private void deleteFilesNotInRTM(String dirPath, Set<String> rtmSet, String fileType) {
        File dir = new File(dirPath);
        int deleteFileNum = 0;
        int allFileNum = 0;
        for (File f : dir.listFiles()) {
            String id = f.getName().split(".txt")[0];
            // warning!! jsp文件名 存在“-”字符的编码问题
            id = id.replace("‐", "-");
            allFileNum++;
            if (!rtmSet.contains(id)) {
                deleteFileNum++;
                f.delete();
//                System.out.printf("Delete %s %s\n", fileType, f.getName());
            }
        }
        System.out.printf("Delete %d of %d %s files which are not in RTM.\n", deleteFileNum, allFileNum, fileType);
    }
}
