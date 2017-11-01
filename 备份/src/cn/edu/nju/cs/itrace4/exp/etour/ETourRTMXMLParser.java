package cn.edu.nju.cs.itrace4.exp.etour;

import cn.edu.nju.cs.itrace4.core.document.Artifact;
import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import cn.edu.nju.cs.itrace4.io.XMLParser;
import cn.edu.nju.cs.itrace4.io._;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by niejia on 15/12/18.
 */
public class ETourRTMXMLParser {
    public static void main(String[] args) {
        ArtifactsCollection collection = XMLParser.createArtifacts(ETOUR_CONSTANTS.rtmXMLPath);
        System.out.println(collection);

        StringBuilder sb = new StringBuilder();
        Set<String> rtm = new HashSet<>();
        for (Artifact artifact : collection.values()) {
//            sb.append(artifact.id + " " + artifact.text + " 1.0");
//            sb.append("\n");
            rtm.add(artifact.id + " " + artifact.text + " 1.0");
        }

        for (String s : rtm) {
            sb.append(s);
            sb.append("\n");
        }
        _.writeFile(sb.toString(), "data/exp/eTour/rtm/RTM_Class.txt");
    }
}
