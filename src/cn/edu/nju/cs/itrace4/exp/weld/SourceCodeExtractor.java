package cn.edu.nju.cs.itrace4.exp.weld;

import java.io.File;

import cn.edu.nju.cs.itrace4.util.io._;

/**
 * Created by niejia on 16/3/11.
 */
public class SourceCodeExtractor {


    public SourceCodeExtractor(String projectPath, String srcPath) {
        File projectFile = new File(projectPath);

        for (File f : projectFile.listFiles()) {
            String filePath = f.getPath();
            if (filePath.endsWith(".java")) {
                String packageName = getPackageName(filePath);
                String fileName = f.getName().split("\\.")[0].split(" ")[0];

                String wholeName = "";
                if (packageName.equals("")) {
                    wholeName = fileName;
                } else {
                    wholeName = packageName + "." + fileName;
                }

                String lowerCaseName = wholeName.toLowerCase();
                _.writeFile(_.readFile(f.getPath()), "data/exp/weld/src/"+lowerCaseName+".java");
            }
        }

    }

    private String getPackageName(String filePath) {
        String inputs = _.readFile(filePath);
        String[] lines = inputs.split("\n");

        String packageName = "";

        for (String line : lines) {
            if (line.startsWith("package ")) {
                String tmp = line.split(" ")[1];
                packageName = tmp.substring(0, tmp.length() - 1);

                break;
            }
        }

        return packageName;
    }

    public static void main(String[] args) {
        String projectPath = "data/exp/weld/project";
        String srcPath = "data/exp/weld/src";

        SourceCodeExtractor extractor = new SourceCodeExtractor(projectPath, srcPath);
    }
}
