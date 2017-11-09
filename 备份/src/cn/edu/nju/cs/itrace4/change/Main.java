package cn.edu.nju.cs.itrace4.change;

import cn.edu.nju.cs.itrace4.io._;

/**
 * Created by niejia on 15/7/7.
 */
public class Main {

    public static String path = "data/exp/iTrust version/iTrust v10.0/requirements v18";

    public static void main(String[] args) {
        int i = 1;
        while (i <= 41) {
            _.writeFile("", path + "/UC" + i + ".txt");
            i++;
        }
    }

}
