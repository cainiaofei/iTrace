package cn.edu.nju.cs.itrace4.util.parser;

import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.util.io._;

import java.io.File;

/**
 * Created by niejia on 15/2/22.
 */
public class JspTextParser {
    private String path;

    public JspTextParser(String path) {
        this.path = path;
    }

    public void exportParsedJspClass() {
        exportParsedJsp(Granularity.CLASS);
    }

    public void exportParsedJspMethod() {
        exportParsedJsp(Granularity.METHOD);
    }

    private void exportParsedJsp(Granularity granularity) {
        File file = new File(path);
        String outputDir = file.getParentFile().getParent();

        String jspName = transformJspName(file.getName(), granularity);

        File JspDir = null;
        if (granularity.equals(Granularity.CLASS)) {
            JspDir = new File(outputDir + "/class/code");
        } else if (granularity.equals(Granularity.METHOD)) {
            JspDir = new File(outputDir + "/method/code");
        }

        // warning!! jsp文件名 存在“-”字符的编码问题
        jspName = jspName.replace("‐", "-");

        String filePath = JspDir.getPath() + "/" + jspName + ".txt";

       // remove jsp stopwords first [Keng]
        String jspStopwordsPath = "data/exp/iTrust/stopwords/stop-words_jsp.txt";
//        _.writeFile(Stopwords.remover(_.readFile(path), jspStopwordsPath), filePath);
                _.writeFile(_.readFile(path), filePath);
    }

    // util.getUser.jsp -> util.getUser_jsp
    private String transformJspName(String name, Granularity granularity) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = name.split("\\.");
        for (int i = 0; i < tokens.length - 2; i++) {
            sb.append(tokens[i]);
            sb.append(".");
        }
        sb.append(tokens[tokens.length - 2]);
        sb.append("_jsp");
        if (granularity.equals(Granularity.METHOD)) {
            sb.append("#_jspService");
        }
        return sb.toString();
    }
}
