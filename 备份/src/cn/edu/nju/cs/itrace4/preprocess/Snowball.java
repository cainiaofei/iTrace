package cn.edu.nju.cs.itrace4.preprocess;

import cn.edu.nju.cs.itrace4.preprocess.snowball.EnglishStemmer;

/**
 * Created by niejia on 15/2/23.
 */
public class Snowball {

    public static String stemming(String input) {
        StringBuilder sb = new StringBuilder();

        String words[] = input.split(" ");

        EnglishStemmer stemmer = new EnglishStemmer();

        for (String word : words) {
            stemmer.setCurrent(word);
            stemmer.stem();
            sb.append(stemmer.getCurrent());
            sb.append(" ");
        }

        return sb.toString();
    }
}
