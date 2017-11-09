package cn.edu.nju.cs.itrace4.core.metrics;

/**
 * Created by niejia on 15/2/23.
 */
public class LinksDistributed {
    public int TP;
    public int FP;
    public int TN;
    public int FN;

    public LinksDistributed(int TP, int FP, int TN, int FN) {
        this.TP = TP;
        this.FP = FP;
        this.TN = TN;
        this.FN = FN;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TP = " + TP);
        sb.append("\n");
        sb.append("FP = " + FP);
        sb.append("\n");
        sb.append("TN = " + TN);
        sb.append("\n");
        sb.append("FN = " + FN);
        sb.append("\n");

        return sb.toString();
    }
}
