package cn.edu.nju.cs.itrace4.visual.presentation.gui;


import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
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
public class CodeDependencyDisplay {
	Map<String,Integer> rank = new HashMap<String,Integer>();
	
    private RelationGraph relationGraph;

    private Graph<Integer, Integer> g;
    private final Factory<Integer> edgeFactory;
    private final Map<Integer, Number> edgeRelationWeightsMap;

    private final Map<Integer, String> vertexNameMap;
    private final Map<String, Integer> vertexIndexMap;

    private TextDataset textDataset;
    private SimilarityMatrix matrix;
    private String LAYOUT_FILE;
    private LinksList qualityScoreLinksList;
    private LinksList highestScoreLinksList;
    private String highestScoreTarget;
    private String secondHighestScoreTarget;
    private String firstValidHighestScoreTarget;
    private PersistentLayout<Integer, Integer> persistentLayout;
    private VisualizationViewer<Integer, Integer> vv;
    private String currentUC = "";
    private List<String> ucRelatedCodes;
    private List<String> ucHighScoresCodes;
    private List<String> ucList;
    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;
    private Map<Integer, Pair<Integer, Integer>> callEdges;
    private Map<Integer, Pair<Integer, Integer>> dataEdges;
    private Map<Integer, Pair<Integer, Integer>> call_data_Edges;
    private Result result;
    
    private JFrame jf;

    public CodeDependencyDisplay(TextDataset textDataset, RelationGraph relationGraph,
    		String layoutPath,JFrame jf) {
    	this.jf = jf;
        this.relationGraph = relationGraph;
        this.textDataset = textDataset;
        this.result = IRForVisual.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        
        this.qualityScoreLinksList = result.getMatrix().getQualityLinks();
        this.highestScoreLinksList = result.getMatrix().getHighestLinks();
        this.pruningInfo = new PruningInfo(textDataset, relationGraph.getRelationInfo());
        this.subGraphInfo = new SubGraphInfo(textDataset, this.relationGraph, result.getMatrix());

        vertexNameMap = new HashMap<>();
        vertexIndexMap = new HashMap<>();
        edgeRelationWeightsMap = new HashMap<Integer,Number>();

        ucRelatedCodes = new ArrayList<>();
        ucHighScoresCodes = new ArrayList<>();
        ucList = new ArrayList<>();

        callEdges = new LinkedHashMap<>();
        dataEdges = new LinkedHashMap<>();
        call_data_Edges = new LinkedHashMap<>();

        this.LAYOUT_FILE = layoutPath;

        edgeFactory = new Factory<Integer>() {
            int i = 1;

            @Override
            public Integer create() {
                return i++;
            }
        };
    }

    private void graphConvert() {
    	g = new SparseGraph<Integer,Integer>();
        Map<Integer, CodeVertex> vertexMap = relationGraph.getVertexes();
        for (Integer id : vertexMap.keySet()) {
            g.addVertex(id);
            vertexNameMap.put(id, vertexMap.get(id).getName());
            vertexIndexMap.put(vertexMap.get(id).getName(), id);
        }

        for (CodeEdge codeEdge : relationGraph.getCallEdges()) {
            Integer v1 = relationGraph.getVertexIdByName(codeEdge.getSource().getName());
            Integer v2 = relationGraph.getVertexIdByName(codeEdge.getTarget().getName());
            Number weight = ((CallEdge) codeEdge).getCallRelationSize();
            Integer edgeId = edgeFactory.create();
            g.addEdge(edgeId, v1, v2,EdgeType.DIRECTED);
            callEdges.put(edgeId, new Pair<Integer, Integer>(v1, v2));
            if (weight == null) weight = 0.0;
            edgeRelationWeightsMap.put(edgeId, weight);
        }

        for (CodeEdge codeEdge : relationGraph.getDataEdges()) {
            Integer v1 = relationGraph.getVertexIdByName(codeEdge.getSource().getName());
            Integer v2 = relationGraph.getVertexIdByName(codeEdge.getTarget().getName());
            Number weight = ((DataEdge) codeEdge).getDataRelationSizeByUniqueType();
            Integer edgeId = edgeFactory.create();
            g.addEdge(edgeId, v1,v2,EdgeType.UNDIRECTED);
            if (weight == null) weight = 0.0;
            edgeRelationWeightsMap.put(edgeId, weight);
        }
    }

    public void show() {
        graphConvert();

        persistentLayout = new PersistentLayoutImpl<Integer, Integer>(new FRLayout<Integer, Integer>(g));
        vv = new VisualizationViewer<Integer, Integer>(persistentLayout);

        matrix = result.getMatrix();

        vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer, String>() {
            public String transform(Integer v) {
            	String name = vertexNameMap.get(v);
            	if(!rank.isEmpty()) {
            		return name;
            		//return scoreStr+"_"+rank.get(name);
            	}
            	else {
            		return "";
            	}
            }
        });

        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Integer, String>() {
            public String transform(Integer eg) {
                return edgeRelationWeightsMap.get(eg).toString();
            }
        });

        vv.getRenderContext().setVertexIconTransformer(new Transformer<Integer, Icon>() {

            public Icon transform(final Integer cv) {
                return new Icon() {

                    public int getIconHeight() {
                        return 20;
                    }

                    public int getIconWidth() {
                        return 20;
                    }

                    public void paintIcon(Component c, Graphics g,
                                          int x, int y) {
                    	String code = vertexNameMap.get(cv);
                    	//g.setColor(Color.blue);
                    	g.setColor(Color.white);
                    	g.fillRect(x, y, code.length()*7+10, 20);
                    	g.setColor(Color.black);
                    	//暂时更改成矩形
                    	g.drawRect(x, y, code.length()*7+10, 20);//150
//                    	g.setColor(Color.white);
//                    	g.fillRect(x, y, code.length()*7+10, 20);
                        //g.fillOval(x, y, 30, 30);
                        if (vv.getPickedVertexState().isPicked(cv)) {
                            g.setColor(Color.red);
                        } else {
                            g.setColor(Color.black);
                        }
                        String similarity = "";
                       
                        if (!currentUC.equals("") && ucRelatedCodes.contains(code)) {
                            similarity = String.valueOf(matrix.getScoreForLink(currentUC, code));
                        }
                        g.setColor(Color.black);
                        g.drawString("" + code, x + 6, y + 15);
                        //g.drawString("" + code, x , y );
                    }
                };
            }
        });

        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white, Color.yellow));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Integer>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        vv.setBackground(Color.white);

        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());

        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        
        //content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer, Integer>();
        vv.setGraphMouse(gm);

        final JButton persist = new JButton("Save Layout");

        JButton restore = new JButton("Restore Layout");
        restore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    persistentLayout.restore(LAYOUT_FILE);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        
        JPanel controls = new JPanel();
        controls.add(persist);
        controls.add(restore);
       // controls.add(ucBox);
        controls.add(((DefaultModalGraphMouse<Integer, Integer>) gm).getModeComboBox());
        
        JLabel callThresholdLabel = new JLabel("call Threshold:");
		JTextField callValue = new JTextField();
		callValue.setText("call threshold");
		//controls.add(callThresholdLabel);
		controls.add(callValue);
		
		JLabel dataThresholdLabel = new JLabel("data Threshold:");
		JTextField dataValue = new JTextField();
		//controls.add(dataThresholdLabel);
		dataValue.setText("data threshold");
		controls.add(dataValue);
		
		JButton update = new JButton("update");
        controls.add(update);
		
        JPanel right = new JPanel(new BorderLayout());
        JLabel label = new JLabel("代码文本展示");
        label.setFont(new Font(null,Font.BOLD,20));
        
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        //JTextPane ta = new JTextPane();
        //ta.setBounds(160,100,200,200);
        right.add(label, BorderLayout.NORTH);
        right.add(new JLabel());
        right.add(new JScrollPane(ta));
        
        
        //JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controls, new JPanel());
        JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, right);
        jsp.setDividerLocation(0.6);// 在1/2处进行拆分
        
        //JSplitPane jsPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, controls);
        
        
        content.add(jsp,BorderLayout.NORTH);
        content.add(controls, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
    }

    public static void showCodeDependencyGraph(JFrame jf) throws IOException {
        String class_relationInfo = "data/exp/iTrust/relation/CLASS_relationInfo_whole.ser";
        
        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();
            ri.setPruning(0.4, 0.8);
            ois.close();
            
            String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/iTrust/uc";
            String classDirPath = "data/exp/iTrust/class/graph/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);
            
            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);
            String layoutPath = "data/exp/iTrust/relation/PersistentLayoutDemo.out";
            CodeDependencyDisplay codeDependencyDisplay = new CodeDependencyDisplay(textDataset, cdGraph,
            		layoutPath,jf);
            codeDependencyDisplay.show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
