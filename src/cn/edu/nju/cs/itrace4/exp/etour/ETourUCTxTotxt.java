package cn.edu.nju.cs.itrace4.exp.etour;

import cn.edu.nju.cs.itrace4.io._;

import java.io.File;

/**
 * Created by niejia on 15/12/20.
 */
public class ETourUCTxTotxt {
    public static void main(String[] args) {
        File dir = new File("data/exp/eTour/UC-2");
        for (File f : dir.listFiles()) {
            _.writeFile(_.readFile(f.getPath()), "data/exp/eTour/AC/" + f.getName().split("\\.")[0] + ".txt");
        }
    }
}
