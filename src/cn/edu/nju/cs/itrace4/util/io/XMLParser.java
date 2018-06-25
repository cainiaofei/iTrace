package cn.edu.nju.cs.itrace4.util.io;

import cn.edu.nju.cs.itrace4.core.document.Artifact;
import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Iterator;

/**
 * Created by niejia on 15/2/23.
 */
public class XMLParser {

    public XMLParser() {
    }

    public static ArtifactsCollection createArtifacts(String path) {
        ArtifactsCollection ac = new ArtifactsCollection();
        SAXReader reader = new SAXReader();

        try {

            Document document = reader.read(new File(path));
            Element root = document.getRootElement();
            Iterator iter = root.elementIterator(Constants.ARTIFACTS);

            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                Iterator i = e.elementIterator(Constants.ARTIFACT);

                while (i.hasNext()) {
                    Element itemEle = (Element) i.next();
                    String id = itemEle.elementTextTrim(Constants.ID);
                    String content = itemEle.elementTextTrim(Constants.CONTENT);

                    Artifact artifact = new Artifact(id, content);
                    ac.put(artifact.id, artifact);
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return ac;
    }
}
