package cn.edu.nju.cs.itrace4.preprocess;

import cn.edu.nju.cs.itrace4.io._;

import java.util.Arrays;
import java.util.List;

/**
 * Created by niejia on 15/2/23.
 */
public class Stopwords {

    public static String remover(String input, String stopwordsPath) {

        String stopwords[] = _.readFile(stopwordsPath).split("\n");
        // about\r remove that \r
        for (int i = 0; i < stopwords.length; i++) {
            stopwords[i] = stopwords[i].trim();
        }
        List<String> stopwordsList = Arrays.asList(stopwords);

        String words[] = input.split(" ");

        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!stopwordsList.contains(word)) {
                sb.append(word);
                sb.append(" ");
            } else {
//                System.out.println("Delete word " + word);
            }
        }
        return sb.toString();
    }
}
