package cn.edu.nju.cs.itrace4.tool.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlParse {
	
	//return a array:two elements projectName and modelName
	public String[] process() throws ParserConfigurationException, SAXException, IOException{
		String[] res = new String[2];
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.parse(new File("resource/config.xml"));
		NodeList configList = doc.getElementsByTagName("config");
		
		//it ensures the configList is not null through input.
		Node config = configList.item(0);
		
		NodeList childList = config.getChildNodes();
		
		for(int i = 0; i < childList.getLength();i++){
			Node curNode = childList.item(i);
			if(curNode.getNodeName().equals("projectName")){
				res[0] = curNode.getTextContent();
			}
			else if(curNode.getNodeName().equals("modelName")){
				res[1] = curNode.getTextContent();
			}
			else{}
		}
		
		return res;
	}
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		XmlParse xmlParse = new XmlParse();
		xmlParse.process();
	}
}
