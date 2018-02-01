package cn.edu.nju.cs.itrace4.util;

/**
 * Created by niejia on 16/1/6.
 */
public class Setting {

    /**
     * 1.4
     * 0.7
     * 1.0
     */


    public static double idfThreshold = 1.4;
    //recently used

//    public static double idfThreshold = 2.0;

//    public static double idfThreshold = 2.5;
//    public static double idfThreshold = 2.5;
//    public static double idfThreshold = 2.5;

    public static double callThreshold = 0.7;
    public static double dataThreshold = 0.9;
//    public static double dataThreshold = 0.2;

//    public static final int LSI_K = 125;
    //public static final int LSI_K = 90;///100
      public static final int LSI_K = 85;///100
//    public static final int LSI_K = 105;

    public static String serializeName = "relationInfo_whole.ser";
//    public static String serializeName = "relationInfo.ser";
}
