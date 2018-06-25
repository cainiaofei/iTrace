package cn.edu.nju.cs.itrace4.preprocess;

import java.io.File;

import cn.edu.nju.cs.itrace4.util.io._;

/**
 * Created by niejia on 15/2/23.
 */
public class BatchingPreprocess {
    private File ucDirPath;
    private File classDirPath;
    private File methodDirPath;

    public BatchingPreprocess() {

    }

    public BatchingPreprocess(String ucDirPath, String classDirPath, String methodDirPath) {
        this.ucDirPath = new File(ucDirPath);
        this.classDirPath = new File(classDirPath);
        this.methodDirPath = new File(methodDirPath);
    }

    public void doProcess() {
    	
        /**
    	 * 也按照源代码那套处理 
    	 * @date 2017/10/15
    	 */
        preprocessJavaAndJsPFiles(ucDirPath);
        preprocessUCFiles(ucDirPath);
        
        preprocessJavaAndJsPFiles(classDirPath);
        preprocessJavaAndJsPFiles(methodDirPath);

    }

    private void preprocessJavaAndJsPFiles(File dirPath) {
        if (dirPath.isDirectory()) {
            for (File f : dirPath.listFiles()) {
                if (f.getName().endsWith("_jsp.txt") || f.getName().endsWith("_jspService.txt")) {
                    TextPreprocessor textPreprocessor = new TextPreprocessor(_.readFile(f.getPath()));
                    _.writeFile(textPreprocessor.doJspFileProcess(), f.getPath());
                } else if (f.getName().endsWith(".txt")) {
                    TextPreprocessor textPreprocessor = new TextPreprocessor(_.readFile(f.getPath()));
                    _.writeFile(textPreprocessor.doJavaFileProcess(), f.getPath());
                }
            }
        }
    }

    public void preprocessUCFiles(File ucDirPath) {
        if (ucDirPath.isDirectory()) {
            for (File f : ucDirPath.listFiles()) {
                if (f.getName().endsWith(".txt")) {
                    TextPreprocessor textPreprocessor = new TextPreprocessor(_.readFile(f.getPath()));
                    _.writeFile(textPreprocessor.doUCFileProcess(), f.getPath());
                }
            }
        }
    }
}
