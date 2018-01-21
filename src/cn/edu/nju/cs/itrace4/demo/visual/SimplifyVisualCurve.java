package cn.edu.nju.cs.itrace4.demo.visual;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.PrecisionRecallCurve;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by niejia on 15/2/24.
 */
public class SimplifyVisualCurve extends MyVisualCurve {

    public static int count = 0;

    private Queue<String> nameQueue = new LinkedList<>();
    protected List<PrecisionRecallCurve> curveList = new ArrayList<>();
    protected Map<PrecisionRecallCurve, String> labelMap = new HashMap<>();
    protected List<Result> resultsInCurve = new ArrayList<>();

    public SimplifyVisualCurve() {
    }

    public SimplifyVisualCurve(PrecisionRecallCurve precisionRecallCurve) {
        curveList.add(precisionRecallCurve);
    }

    public void showChart() {
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.setVisible(true);
    }

    private JPanel createChartPanel() {
        String chartTitle = "Precision-Recall Curve";
        String xAxisLabel = "Recall";
        String yAxisLabel = "Precision";

        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        customizeChart(chart);

        return new ChartPanel(chart);
    }

    protected void customizeChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.green, Color.orange};
        Random rand = new Random();

        for (int i = 0; i < curveList.size(); i++) {
            int index = rand.nextInt(colors.length);
            renderer.setSeriesPaint(0, colors[index]);
        }
    }

    protected XYDataset createDataset() {

        XYSeriesCollection dataSet = new XYSeriesCollection();

        for (PrecisionRecallCurve curve : curveList) {
            //System.out.println(nameQueue.remove());
            List<String> pointId = new ArrayList<>();
            for (String key : curve.keySet()) {
                String id = key.split("_")[0];
                if (!pointId.contains(id)) {
                    pointId.add(id);
                }
            }

            double gap = 0.1;
            
            String label = labelMap.get(curve) + " : " + String.valueOf(count) + "-" + curve.getName() + " at " + curve.getCutParameter();
            count++;
            XYSeries series = new XYSeries(label);
           // System.out.println(curve.getName());
            for (String id : pointId) {
                double p = curve.get(id + "_Precision");

                double r = curve.get(id + "_Recall");
                series.add(r, p);
                if (r > gap) {
                   // System.out.println("Precision: " + p + " @Recall: " + r);
                    gap += 0.1;
                }
            }
            dataSet.addSeries(series);
        }
        return dataSet;
    }

    /**
     * @author zzf
     * @date 2018.1.14
     * @description it will eat too much time in former method. Here we only connect 
     * 	vertex which when valid. 
     */
    public void addLine(Result result) {
        resultsInCurve.add(result);
        curveList.add(getPrecisionRecallCurve(result));
        //找bug开始
//        if(labelMap.containsKey(result.getPrecisionRecallCurve())){
//        	System.out.println("");
//        }
        //找bug结束
        
        String temp = result.getModel() + " " + result.getAlgorithmName();
        
        labelMap.put(getPrecisionRecallCurve(result), result.getModel() + " " + result.getAlgorithmName());
        nameQueue.add(result.getAlgorithmName());
    }

    /**
     * @author zzf
     * @date 2018.1.14
     * @description  
     */
    private PrecisionRecallCurve getPrecisionRecallCurve(Result result) {
    	SimilarityMatrix oracle = result.getOracle();
    	SimilarityMatrix matrix = result.matrix;
    	
    	PrecisionRecallCurve precisionRecallCurve = new PrecisionRecallCurve();
        precisionRecallCurve.setName(result.getAlgorithmName());
        precisionRecallCurve.setCutParameter(result.getCutParameter());
        
        oracle.setThreshold(0.0);
        int correct = 0;

        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;

        LinksList allLinks = matrix.allLinks();
        Collections.sort(allLinks, Collections.reverseOrder());

//        System.out.println(" allLinks = " + allLinks );
        /**
         * it seemed this links doesnt use
         * */
        LinksList links = matrix.getLinksAboveThreshold();
        Collections.sort(links, Collections.reverseOrder());

        int linkNumber = 0;
        for (SingleLink link : allLinks) {
//            System.out.println(" link = " + link );
            String source = link.getSourceArtifactId();
            String target = link.getTargetArtifactId();
            if (matrix.isLinkAboveThreshold(source, target)) {
            	linkNumber++;
                if (oracle.isLinkAboveThreshold(source, target)) {
                    correct++;
                    TP++;
                    double precision = correct / (double) linkNumber;
                    precisionRecallCurve.put(String.format("%03d_Precision", linkNumber), precision);
                    double recall = correct / (double) oracle.count();
                    precisionRecallCurve.put(String.format("%03d_Recall", linkNumber), recall);
                } else {
                    FP++;
                }
            } else {
                if (oracle.isLinkAboveThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
                    FN++;
                }
                TN++;
            }
        }
        return precisionRecallCurve;
	}

	public static BufferedImage getScreenShot(
            Component component) {

        BufferedImage image = new BufferedImage(
                component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint( image.getGraphics() ); // alternately use .printAll(..)
        return image;
    }

    public void curveStore(String path) throws IOException {
        String curveName = "PrecisionRecallCurve";
        BufferedImage img = getScreenShot(
                this.getContentPane());
        ImageIO.write(img, "png", new File(path + "/" + curveName + ".png"));
    }
    
    public void curveStore(String path,String name) throws IOException {
        String curveName = name;
        Component component = this.getContentPane();
        BufferedImage img = getScreenShot(component);
        ImageIO.write(img, "png", new File(path + "/" + curveName + ".png"));
    }
    

    public void resultStore(String expExportPath, String expName) throws IOException {
        String dirName = expName;

        File dir = new File(expExportPath + "/" + dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }

        metricsStore(dir.getPath());
        curveStore(dir.getPath());

    }

    private void metricsStore(String path) {
        String[] xyzabc = {"x", "y", "z", "a", "b", "c"};
        int i = 0;
        for (Result result : resultsInCurve) {
            result.store(path, xyzabc[i]);
            i++;
        }
    }
}
