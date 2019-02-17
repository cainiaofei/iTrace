package cn.edu.nju.cs.itrace4.visual;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.relation.graph.CallEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import cn.edu.nju.cs.itrace4.relation.graph.DataEdge;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
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
 * Created by niejia on 15/12/30.
 */
public class VisualRelationGraph_RE {

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

    private String initialRegionSeed;

    //    private LinksList highestScoreLinksList;
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
    private SimilarityMatrix rtm;

//    public VisualRelationGraph_RE(TextDataset textDataset, RelationGraph relationGraph,Result result, String layoutPath) {
//        this.relationGraph = relationGraph;
//        this.textDataset = textDataset;
//        this.result = result;
//        this.qualityScoreLinksList = result.getMatrix().getQualityLinks();
//        this.highestScoreLinksList = result.getMatrix().getHighestLinks();
//        this.pruningInfo = new PruningInfo(textDataset, relationGraph.getRelationInfo());
////        this.subGraphInfo = new SubGraphInfo(textDataset, relationGraph.getRelationInfo());
//        this.subGraphInfo = new SubGraphInfo(textDataset, this.relationGraph);
//        vertexNameMap = new HashMap<>();
//        vertexIndexMap = new HashMap<>();
//        edgeRelationWeightsMap = new HashMap<>();
//
//        ucRelatedCodes = new ArrayList<>();
//        ucHighScoresCodes = new ArrayList<>();
//        ucList = new ArrayList<>();
//
//        callEdges = new LinkedHashMap<>();
//        dataEdges = new LinkedHashMap<>();
//        call_data_Edges = new LinkedHashMap<>();
//
////        this.LAYOUT_FILE = "data/exp/iTrust/relation/PersistentLayoutDemo.out";
//        this.LAYOUT_FILE = layoutPath;
//
//        edgeFactory = new Factory<Integer>() {
//            int i = 1;
//
//            @Override
//            public Integer create() {
//                return i++;
//            }
//        };
//    }

    public VisualRelationGraph_RE(TextDataset textDataset, RelationGraph relationGraph, String layoutPath, String model) {
        this.relationGraph = relationGraph;
        this.textDataset = textDataset;
        this.result = IR.compute(textDataset, model, new None_CSTI());
        this.qualityScoreLinksList = result.getMatrix().getQualityLinks();
        this.highestScoreLinksList = result.getMatrix().getHighestLinks();
        this.pruningInfo = new PruningInfo(textDataset, relationGraph.getRelationInfo());
        this.subGraphInfo = new SubGraphInfo(textDataset, this.relationGraph, result.getMatrix());



        vertexNameMap = new HashMap<>();
        vertexIndexMap = new HashMap<>();
        edgeRelationWeightsMap = new HashMap<>();

        ucRelatedCodes = new ArrayList<>();
        ucHighScoresCodes = new ArrayList<>();
        ucList = new ArrayList<>();

        callEdges = new LinkedHashMap<>();
        dataEdges = new LinkedHashMap<>();
        call_data_Edges = new LinkedHashMap<>();

//        this.LAYOUT_FILE = "data/exp/iTrust/relation/PersistentLayoutDemo.out";
        this.LAYOUT_FILE = layoutPath;

        this.rtm = textDataset.getRtm();

        edgeFactory = new Factory<Integer>() {
            int i = 1;

            @Override
            public Integer create() {
                return i++;
            }
        };
    }

    private void graphConvert() {
        g = new DirectedSparseGraph();

        Map<Integer, CodeVertex> vertexMap = relationGraph.getVertexes();

        Set<String> vertexInRTM = rtm.targetArtifactsIds();

        for (Integer id : vertexMap.keySet()) {

//            if (vertexInRTM.contains(vertexMap.get(id))) {
                g.addVertex(id);
                vertexNameMap.put(id, vertexMap.get(id).getName());
                vertexIndexMap.put(vertexMap.get(id).getName(), id);
//            }


        }

        for (CodeEdge codeEdge : relationGraph.getCallEdges()) {
            Integer v1 = relationGraph.getVertexIdByName(codeEdge.getSource().getName());
            Integer v2 = relationGraph.getVertexIdByName(codeEdge.getTarget().getName());

            Number weight = ((CallEdge) codeEdge).getCallRelationSize();

            Integer edgeId = edgeFactory.create();
            g.addEdge(edgeId, v1, v2);
            callEdges.put(edgeId, new Pair<Integer, Integer>(v1, v2));
            if (weight == null) weight = 0.0;
            edgeRelationWeightsMap.put(edgeId, weight);
        }

        for (CodeEdge codeEdge : relationGraph.getDataEdges()) {
            Integer v1 = relationGraph.getVertexIdByName(codeEdge.getSource().getName());
            Integer v2 = relationGraph.getVertexIdByName(codeEdge.getTarget().getName());

//            Number weight = ((DataEdge) codeEdge).getSharedDataFieldSize();
            Number weight = ((DataEdge) codeEdge).getDataRelationSizeByUniqueType();
            Integer edgeId = edgeFactory.create();
            g.addEdge(edgeId, v1, v2);
//            dataEdges.put(edgeId, new Pair<Integer, Integer>(v1, v2));
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
                String similarity = "";
                String code = vertexNameMap.get(v);

                if (!currentUC.equals("")) {
                    similarity = String.valueOf(matrix.getScoreForLink(currentUC, code));
                }
                return (vertexNameMap.get(v)) + "\n" + similarity;
            }
        });

        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Integer, String>() {
            public String transform(Integer eg) {
//                    if (currentUC.equals("")) return "";
//                    if (ucRelatedCodes.size()==0) return "";
//                    if (ucRelatedCodes.contains())
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
                        if (vv.getPickedVertexState().isPicked(cv)) {
                            g.setColor(Color.yellow);
                        } else if (ucRelatedCodes.size() != 0) {
                            String v = vertexNameMap.get(cv);

                            // if initial_seed & inRTM
                            if (initialRegionSeed.equals(v) & ucRelatedCodes.contains(v)) {
                                g.setColor(Color.RED);
                            } else if (initialRegionSeed.equals(v)) {
                                g.setColor(Color.CYAN);
                            } else if (highestScoreTarget.equals(v) && ucRelatedCodes.contains(v)) {
                                g.setColor(Color.MAGENTA);
                            } else if (highestScoreTarget.equals(v))  {
                                g.setColor(Color.GRAY);
                            } else if (ucRelatedCodes.contains(v)) {
                                g.setColor(Color.BLUE);
                            } else {
                                g.setColor(Color.BLACK);
                            }
                        } else {
                            g.setColor(Color.BLACK);
                        }
                        g.fillOval(x, y, 30, 30);
                        if (vv.getPickedVertexState().isPicked(cv)) {
                            g.setColor(Color.black);
                        } else {
                            g.setColor(Color.white);
                        }
                        String similarity = "";
                        String code = vertexNameMap.get(cv);
//                        System.out.println(" code = " + code );
//                        System.out.println(" ucRelatedCodes = " + ucRelatedCodes );
                        if (!currentUC.equals("") && ucRelatedCodes.contains(code)) {
                            similarity = String.valueOf(matrix.getScoreForLink(currentUC, code));
//                            System.out.println(currentUC+ " "+code+" = " + similarity );
                        }
                        g.drawString("" + cv, x + 6, y + 15);
                    }
                };
            }
        });

        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white, Color.yellow));
//        vv.getRenderContext().setVertexFillPaintTransformer(new MyVertexFillPaintFunction<String>());
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Integer>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        vv.setBackground(Color.white);

        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());

        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer, Integer>();
        vv.setGraphMouse(gm);

        final JComboBox ucBox = new JComboBox();
        for (String uc : textDataset.getSourceCollection().keySet()) {
            ucBox.addItem(uc);
            ucList.add(uc);
        }

        ucBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox type = (JComboBox) e.getSource();
                String uc = (String) type.getSelectedItem();
                currentUC = uc;
                ucRelatedCodes = new ArrayList<String>();
                ucHighScoresCodes = new ArrayList<String>();

                for (SingleLink link : highestScoreLinksList) {
                    if (link.getSourceArtifactId().equals(currentUC)) {
                        highestScoreTarget = link.getTargetArtifactId();
                        break;
                    }
                }

                LinksList startLinks = new LinksList();
                System.out.println("Starts");
                for (CodeVertex cv : ((CallDataRelationGraph) relationGraph).getStartVertexes()) {
                    SingleLink singleLink = new SingleLink(currentUC, cv.getName(), result.getMatrix().getScoreForLink(currentUC, cv.getName()));
                    startLinks.add(singleLink);
                }
                Collections.sort(startLinks, Collections.reverseOrder());
                int i = 0;
//                while (i < 5) {
//                    System.out.println(startLinks.get(i));
//                    i++;
//                }

                LinksList endLinks = new LinksList();
                System.out.println();
                System.out.println("Ends");
                for (CodeVertex cv : ((CallDataRelationGraph) relationGraph).getEndVertexes()) {
                    SingleLink singleLink = new SingleLink(currentUC, cv.getName(), result.getMatrix().getScoreForLink(currentUC, cv.getName()));
                    endLinks.add(singleLink);
                }
                Collections.sort(endLinks, Collections.reverseOrder());
                int j = 0;
//                while (j < 5) {
//                    System.out.println(endLinks.get(j));
//                    j++;
//                }
//                List<String> fp = new ArrayList<String>();
//                subGraphInfo.getFirstPieceCodeForSource(currentUC, fp, UseEdge.Call);
//                secondHighestScoreTarget = result.getMatrix().getSecondMaxValueTarget(currentUC, fp);
                secondHighestScoreTarget = result.getMatrix().getSecondMaxValueTarget(currentUC, pruningInfo.getFirstPieceCodeForSource(currentUC));

                List<String> tmp = new ArrayList<String>();
                initialRegionSeed = subGraphInfo.getFirstValidPieceCodeForSource(currentUC, tmp, UseEdge.Call);

                if (highestScoreTarget == null || highestScoreTarget.equals("")) {
                    throw new NoSuchElementException("Can't find highest Target for Source " + currentUC + ".");
                }

                for (SingleLink link : qualityScoreLinksList) {
                    if (link.getSourceArtifactId().equals(currentUC)) {
                        ucHighScoresCodes.add(link.getTargetArtifactId());
                    }
                }

                for (SingleLink link : textDataset.getRtm().getLinksAboveThresholdForSourceArtifact(uc)) {
                    ucRelatedCodes.add(link.getTargetArtifactId());
                }

//                List<Integer> edgesToRemove = new ArrayList<Integer>();
//                for (Integer edge : g.getEdges()) {
//                    int highestTargetVertexId = vertexIndexMap.get(highestScoreTarget);
//                    System.out.println(" highestTargetVertexId = " + highestTargetVertexId );
//                    edu.uci.ics.jung.graph.util.Pair<Integer> pair = g.getEndpoints(edge);
//                    if (pair.getFirst() != highestTargetVertexId && pair.getSecond() != highestTargetVertexId) {
//                        System.out.println("haha");
//                        edgesToRemove.add(edge);
//                    }
//                }
//
//                for (Integer i : edgesToRemove) {
//                    g.removeEdge(i);
//                }
                vv.repaint();
            }
        });

        final JButton persist = new JButton("Save Layout");
        persist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    persistentLayout.persist(LAYOUT_FILE);
                } catch (IOException e1) {
                    System.err.println("got " + e1);
                }
            }
        });

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
        controls.add(ucBox);
        controls.add(((DefaultModalGraphMouse<Integer, Integer>) gm).getModeComboBox());
        content.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

    }

    public static void main(String[] args) throws IOException {
        String class_relationInfo = "data/exp/iTrust/relation/Class_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();
            ri.setPruning(2, 0.2);

            System.out.println(ri.getRelationGraphFile());

            String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/iTrust/uc";
            String classDirPath = "data/exp/iTrust/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);

            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);
            String layoutPath = "data/exp/iTrust/relation/PersistentLayoutDemo.out";
            VisualRelationGraph visualRelationGraph = new VisualRelationGraph(textDataset, cdGraph, layoutPath);
            visualRelationGraph.show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
