package cn.edu.nju.cs.itrace4.core.ir.lsi;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * Created by niejia on 16/3/9.
 */
public class SVD {

    public static RealMatrix compute(RealMatrix matrix, int k) {
        return rebuildMatrixBySVD(matrix, k);
    }

    public static RealMatrix rebuildMatrixBySVD(RealMatrix matrix, int k) {

        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();

        RealMatrix u_k = getFirstKColumns(u, k);
        RealMatrix s_k = getLargestKForS(s, k);
        RealMatrix v_k = getFirstKColumns(v, k);

        RealMatrix result = u_k.multiply(s_k).multiply(v_k.transpose());
        return result;
    }
    public static RealMatrix getLargestKForS(RealMatrix s, int k) {
        return s.getSubMatrix(0, k - 1, 0, k - 1);
    }

    public static RealMatrix getFirstKColumns(RealMatrix u, int k) {
        return u.getSubMatrix(0, u.getRowDimension()-1, 0, k-1);
    }
}
