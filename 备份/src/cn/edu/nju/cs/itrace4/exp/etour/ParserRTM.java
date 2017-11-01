package cn.edu.nju.cs.itrace4.exp.etour;

import cn.edu.nju.cs.itrace4.io._;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by niejia on 15/12/20.
 */
public class ParserRTM {
    public static void main(String[] args) {
        String str = "data/exp/eTour/rtm/UseCase-Classes-Links.txt";

        String input = _.readFile(str);
        System.out.println(input);

        StringBuilder sb = new StringBuilder();

        String[] lines = input.split("\n");
        Set<String> rtm = new HashSet<>();

        for (String line : lines) {
            String[] tokens = line.split(" ");
            System.out.println(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
//                sb.append(tokens[0] + " " + tokens[i] + " " + "1.0\n");
                rtm.add(tokens[0] + " " + tokens[i] + " " + "1.0");
            }
        }


        for (String s : rtm) {
            sb.append(s);
            sb.append("\n");
        }

        _.writeFile(sb.toString(), "data/exp/eTour/rtm/RTM_Class.txt");
    }
}
