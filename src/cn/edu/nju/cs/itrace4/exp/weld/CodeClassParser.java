package cn.edu.nju.cs.itrace4.exp.weld;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by niejia on 16/3/10.
 */
public class CodeClassParser {

    private Map<String, String> hashClassMap;

    public CodeClassParser(String filePath) throws IOException {

        Reader in = new FileReader(filePath);
        hashClassMap = new LinkedHashMap<>();
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
        for (CSVRecord record : records) {
            String classHash = record.get("CodeClassHash");
            String path = record.get("Path");

            hashClassMap.put(classHash, path);
//            System.out.println(classHash);
//            System.out.println(path);
        }

//        System.out.println(hashClassMap.size());
    }

    public String getClassNameByHashID(String hashID) {
        return hashClassMap.get(hashID);
    }

    public static void main(String[] args) throws IOException {
        String path = "data/exp/weld/archive/codeClasses.csv";
        CodeClassParser parser = new CodeClassParser(path);

    }

}
