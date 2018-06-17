package cn.edu.nju.cs.itrace4.visual.presentation;


import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * @author zzf
 * @date 2018.3.16
 * @description display the graph of code dependency. 
 */
public class CodeDisplay {
	//private String fileName = "tempCode/HospitalBeanValidator.java";
	private String fileName = "tempCode/UpdateHospitalListAction.java";
	Map<String,Integer> rank = new HashMap<String,Integer>();
    private PersistentLayout<Integer, Integer> persistentLayout;
    private VisualizationViewer<Integer, Integer> vv;
    private String currentUC = "";
    private JFrame jf;
    private FileProcess fileProcess = new FileProcessTool();
    public void show() throws FileException, IOException {
		jf = new JFrame();
        JPanel right = new JPanel(new BorderLayout());
        JLabel label = new JLabel("代码文本展示");
        label.setFont(new Font(null,Font.BOLD,20));
        String content = fileProcess.getFileConent(fileName);
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setText(content);
        //JTextPane ta = new JTextPane();
        //ta.setBounds(160,100,200,200);
        right.add(label, BorderLayout.NORTH);
        right.add(new JLabel());
        right.add(new JScrollPane(ta));
        jf.add(right);
        
        //JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controls, new JPanel());
        jf.pack();
        jf.setVisible(true);
    }

    public static void main(String[] args) throws FileException, IOException {
    	CodeDisplay codeDisplay = new CodeDisplay();
    	codeDisplay.show();
    }
}
