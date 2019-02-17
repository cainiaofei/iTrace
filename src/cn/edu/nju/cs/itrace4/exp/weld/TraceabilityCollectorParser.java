package cn.edu.nju.cs.itrace4.exp.weld;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cn.edu.nju.cs.itrace4.util.io._;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by niejia on 16/3/10.
 */
public class TraceabilityCollectorParser {

    private Map<String,String> requirementsMap;

    public TraceabilityCollectorParser(String filePath) throws IOException {

        Reader in = new FileReader(filePath);
        requirementsMap = new LinkedHashMap<>();
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

        Set<String> set = new HashSet<>();
        for (CSVRecord record : records) {
            String isBug = record.get("IsBug");

            if (isBug.equals("False")) {
                String kind = record.get("Kind");
                if (kind.equals("Feature Request")) {
                    String anchor = record.get("Anchor");
//                    System.out.println(anchor);
                    String summary = record.get("Summary");
//                    System.out.println(summary);
                    requirementsMap.put(anchor, summary);
                }
            }
        }

        for (String req : requirementsMap.keySet()) {
            String content = requirementsMap.get(req);

            _.writeFile(content, "data/exp/weld/uc/" + req + ".txt");
        }

//        System.out.println(set.size());
    }

    public Map<String, String> getRequirementsMap() {
        return requirementsMap;
    }

    public static void main(String[] args) throws IOException {
        String path = "data/exp/weld/archive/TraceabilityCollector.Model.Jira.Issue.csv";
        TraceabilityCollectorParser parser = new TraceabilityCollectorParser(path);
    }
}
