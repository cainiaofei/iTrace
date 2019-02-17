package cn.edu.nju.cs.itrace4.util.io;

import cn.edu.nju.cs.itrace4.core.document.Artifact;
import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;

import java.io.File;

/**
 * Created by niejia on 15/2/10.
 */
public class ArtifactsReader {

    public static ArtifactsCollection getCollections(String dirPath, String postfixName) {
       //System.out.println("dirPath:"+dirPath);
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            _.abort("Artifacts directory doesn't exist");
        }

        if (!dirFile.isDirectory()) {
            _.abort("Artifacts path should be a directory");
        }

        ArtifactsCollection collections = new ArtifactsCollection();
        for (File f : dirFile.listFiles()) {
            if (f.getName().endsWith(postfixName)) {
                String id = f.getName().split(".txt")[0];
                // warning!! jsp文件名 存在“-”字符的编码问题
                id = id.replace("‐", "-");
//                System.out.println(id);

                Artifact artifact = new Artifact(id, _.readFile(f.getPath()));
                collections.put(id, artifact);
            }
        }

//        System.out.println(collections.size() + " " + postfixName + " collections improted.");
        return collections;
    }
}
