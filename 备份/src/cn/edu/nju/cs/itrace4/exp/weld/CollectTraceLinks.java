package cn.edu.nju.cs.itrace4.exp.weld;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by niejia on 16/3/11.
 */
public class CollectTraceLinks {

    public CollectTraceLinks(String codeClassPath, String codeLinkPath, String traceabilityCollectorPath) throws IOException {
        CodeClassParser codeClassParser = new CodeClassParser(codeClassPath);
        CodeLinksParser codeLinksParser = new CodeLinksParser(codeLinkPath);
        TraceabilityCollectorParser traceabilityCollectorParser = new TraceabilityCollectorParser(traceabilityCollectorPath);

        Map<String, String> classTicketMap = codeLinksParser.getClassTicketMap();
        Map<String, String> requirementsMap = traceabilityCollectorParser.getRequirementsMap();

        Set<String> tracelinks = new LinkedHashSet<>();

        for (String classID : classTicketMap.keySet()) {
            String ticketID = classTicketMap.get(classID);
            String className = codeClassParser.getClassNameByHashID(classID);

            if (requirementsMap.keySet().contains(ticketID) && !className.startsWith("tests")) {
//                String trace = ticketID + " " + JavaElement.getIdentifier(className) + " 1.0";
                String trace = ticketID + " " + className + " 1.0";
                tracelinks.add(trace);
            }
        }

        for (String trace : tracelinks) {
            System.out.println(trace);
        }

        System.out.println(tracelinks.size());
    }

    public static void main(String[] args) throws IOException {
        String codeClassPath = "data/exp/weld/archive/codeClasses.csv";
        String codeLinksPath = "data/exp/weld/archive/codeLinks.csv";
        String traceabilityCollectorPath = "data/exp/weld/archive/TraceabilityCollector.Model.Jira.Issue.csv";

        CollectTraceLinks traceLinks = new CollectTraceLinks(codeClassPath, codeLinksPath,traceabilityCollectorPath);
    }
}
