package cn.edu.nju.cs.itrace4.preprocess;

/**
 * Created by niejia on 15/2/22.
 */
public class TextPreprocessor {

    private String str;
    private String stopwordsPath = "data/exp/stopwords/stop-words_english_1_en.txt";
    private String jspStopwordsPath = "data/exp/stopwords/stop-words_jsp.txt";

    public TextPreprocessor(String str) {
        this.str = str;
    }

    public String doUCFileProcess() {
        str = CleanUp.chararctorClean(str);
        // here
//        str = CamelCase.split(str);
        str = CleanUp.lengthFilter(str, 3);
        str = CleanUp.tolowerCase(str);
        str = Snowball.stemming(str);
        str = Stopwords.remover(str, stopwordsPath);
        return str;
    }

    public String doJavaFileProcess() {
        str = CleanUp.chararctorClean(str);
        str = CamelCase.split(str);
        str = CleanUp.lengthFilter(str, 3);
        str = CleanUp.tolowerCase(str);
        str = Snowball.stemming(str);
        str = Stopwords.remover(str, stopwordsPath);
        return str;
    }

    public String doJspFileProcess() {
        str = CleanUp.chararctorClean(str);
        str = Stopwords.remover(str, jspStopwordsPath);
        str = CamelCase.split(str);
        str = CleanUp.lengthFilter(str, 3);
        str = CleanUp.tolowerCase(str);
        str = Snowball.stemming(str);
        str = Stopwords.remover(str, stopwordsPath);
        str = Stopwords.remover(str, jspStopwordsPath);
        return str;
    }
}
