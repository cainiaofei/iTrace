package cn.edu.nju.cs.itrace4.io;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.parser.RTMParser;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/2/10.
 */
public class SqliteIO {

    public static SimilarityMatrix readRTMFromDB(String path, Granularity granularity) {


        File dbFile = new File(path);
        if (!dbFile.exists()) {
            _.abort("DB file doesn't exist");
        }

        Connection con;
        Statement stmt;
        List<String> columnsList = new ArrayList<>();
        String contents = null;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + path);
            con.setAutoCommit(false);

            System.out.printf("Opened %s successfully\n", dbFile.getName());
            stmt = con.createStatement();

            // Store columns from table reqs
            columnsList = getColumnsName(stmt);
            /*
            Store trace links from RTM in text format, such as
            UC1 AuthDAO
             */
            contents = getTracesInRTM(stmt, columnsList);

            System.out.println("Table reqs parsed successfully");
            System.out.printf("Closed %s successfully\n", dbFile.getName());
//            System.out.println(contents);
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("RTM Info: ");
        System.out.printf("columns num: %d\n", columnsList.size());
        System.out.printf("records num: %d\n", contents.split("\n").length);

        SimilarityMatrix sm = RTMParser.createSimilarityMatrix(columnsList, contents, granularity);

        // Export the parsed RTM if you want
        _.writeFile(sm.toString(), dbFile.getParent() + "/RTM_" + granularity + ".txt");
        return sm;
    }

    private static String getTracesInRTM(Statement stmt, List<String> columnsList) {
        StringBuilder sb = new StringBuilder();

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM reqs;");
            while (rs.next()) {

                // rowName: AuthDAO
                String rowName = rs.getString("class");

                // warning!! iTrust-req.db 存在“-”字符的编码问题
                rowName = rowName.replace("‐", "-");
                System.out.println("rowName:"+rowName);
                for (String col : columnsList) {
                    String cell = rs.getString(col).trim();
                    if (!cell.equals("")) {
                        if (cell.equals("x")) {
                            sb.append(col);
                            sb.append(" ");
                            sb.append(rowName);
                            sb.append(" ");
                            sb.append("1");
                            sb.append(" ");
                            sb.append("\n");
                        }
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static List<String> getColumnsName(Statement stmt) {
        List<String> columnsNames = new ArrayList<>();

        try {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(reqs)");
            while (rs.next()) {
                columnsNames.add(rs.getString(2));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        /*
        class
        UC1
        UC2
        ...

        remove the word "class"
         */
        columnsNames.remove(0);
        return columnsNames;
    }
}
