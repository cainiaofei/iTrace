package cn.edu.nju.cs.presentation;


import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.relation.graph.CallEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import cn.edu.nju.cs.itrace4.relation.graph.DataEdge;
import cn.edu.nju.cs.itrace4.visual.IRForVisual;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import edu.uci.ics.jung.visualization.layout.PersistentLayoutImpl;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import javafx.util.Pair;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;

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
