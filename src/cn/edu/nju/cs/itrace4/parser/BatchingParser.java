package cn.edu.nju.cs.itrace4.parser;

import java.io.File;

/**
 * Created by niejia on 15/2/22.
 * Extract Class and method form Java/Jsp File
 */
public class BatchingParser {

    private File srcDir;

    public BatchingParser(String srcDirPath) {
        this.srcDir = new File(srcDirPath);
    }

    public void parse() {
        if (srcDir.isDirectory()) {
            for (File f : srcDir.listFiles()) {
                File[] files = srcDir.listFiles();
                if (f.getName().endsWith(".java")) {
                   // System.out.println(f.getName());
                    JavaTextParser parser = new JavaTextParser(f.getPath());
                    parser.exportParsedClass();
                    parser.exportParsedMethod();
                } else if (f.getName().endsWith(".jsp")) {
                    JspTextParser parser = new JspTextParser(f.getPath());
                    parser.exportParsedJspClass();
                    parser.exportParsedJspMethod();
                }
            }
        }
    }
}
