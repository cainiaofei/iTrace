package cn.edu.nju.cs.itrace4.exp.etour;

import java.io.File;

import cn.edu.nju.cs.itrace4.util.io._;

/**
 * Created by niejia on 15/12/20.
 */
public class ETourClassTxToJava {
    public static void main(String[] args) {
        File dir = new File("data/exp/eTour/CC-2");
        for (File f : dir.listFiles()) {
            _.writeFile(_.readFile(f.getPath()), "data/exp/eTour/class/no/" + f.getName().split("\\.")[0] + ".txt");
        }
    }
}
