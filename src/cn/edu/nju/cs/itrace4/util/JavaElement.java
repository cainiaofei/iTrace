package cn.edu.nju.cs.itrace4.util;

/**
 * Created by niejia on 16/3/11.
 */

/**
 * Created by niejia on 16/1/10.
 */
public class JavaElement {

    public static String getIdentifier(String name) {
        String[] tokens = name.split("\\.");
        return tokens[tokens.length - 1];
    }

    public static String getClassName(String name) {
        String[] tokens = name.split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (Character.isLowerCase(tokens[i].charAt(0))) {
                sb.append(tokens[i]);
                sb.append(".");
            } else {
                sb.append(tokens[i]);
                break;
            }
        }
        return sb.toString();
    }

    public static String getPackageName(String name) {
        String[] tokens = name.split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (Character.isLowerCase(tokens[i].charAt(0))) {
                sb.append(tokens[i]);
                sb.append(".");
            } else {
                break;
            }
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

}
