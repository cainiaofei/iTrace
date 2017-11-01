package cn.edu.nju.cs.itrace4.core.ir.lsi;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by niejia on 16/3/9.
 */
public class Apache {

    public static void main(String[] args) {

        double[][] vals = { {1, 0, 0, 0, 2},
                {0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 4, 0, 0, 0},
        };


        RealMatrix matrix = MatrixUtils.createRealMatrix(vals);
        System.out.println(matrix.getEntry(0, 0));
        System.out.println(matrix.getEntry(3, 1));
//        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
//
//        RealMatrix u = svd.getU();
//        RealMatrix s = svd.getS();
//        RealMatrix v = svd.getV();
//
//        System.out.println("Original");
//        System.out.println(matrix);
//        System.out.println("U");
//        System.out.println(u);
//        System.out.println("S");
//        System.out.println(s);
//        System.out.println("V");
//        System.out.println(v);
//
//        System.out.println("u * ut");
//        System.out.println(u.multiply(u.transpose()));
//        System.out.println("v * vt");
//        System.out.println(v.multiply(v.transpose()));
//
//        System.out.println(u.multiply(s).multiply(v.transpose()));


        System.out.println(SVD.compute(matrix, 2));
//        System.out.println(rebuildMatrixBySVD(matrix, 4));
    }


}
