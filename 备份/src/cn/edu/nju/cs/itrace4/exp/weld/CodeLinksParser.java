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
public class CodeLinksParser {

    private Map<String, String> classTicketMap;

    public CodeLinksParser(String filePath) throws IOException {

        Reader in = new FileReader(filePath);
        classTicketMap = new LinkedHashMap<>();
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
        for (CSVRecord record : records) {
            String codeHash = record.get("CodeHashCode");
            String ticketId = record.get("TicketId");

            classTicketMap.put(codeHash, ticketId);
        }

//        System.out.println(classTicketMap.size());
    }

    public Map<String, String> getClassTicketMap() {
        return classTicketMap;
    }

    public static void main(String[] args) throws IOException {
        String path = "data/exp/weld/archive/codeLinks.csv";
        CodeLinksParser parser = new CodeLinksParser(path);

    }
}
