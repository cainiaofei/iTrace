package cn.edu.nju.cs.itrace4.visual;

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
 * Created by niejia on 16/1/11.
 */
public class VisualCurveFmeasure extends JFrame{
    public static int count = 0;

    private Queue<String> nameQueue = new LinkedList<>();
    List<PrecisionRecallCurve> curveList = new ArrayList<>();
    Map<PrecisionRecallCurve, String> labelMap = new HashMap<>();
    List<Result> resultsInCurve = new ArrayList<>();

    public VisualCurveFmeasure() {
    }

    public VisualCurveFmeasure(PrecisionRecallCurve precisionRecallCurve) {
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
        String chartTitle = "F-measure-Recall Curve";
        String xAxisLabel = "Recall";
        String yAxisLabel = "F-measure";

        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        customizeChart(chart);

        return new ChartPanel(chart);
    }

    private void customizeChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.green, Color.orange};
        Random rand = new Random();

        for (int i = 0; i < curveList.size(); i++) {
            int index = rand.nextInt(colors.length);
            renderer.setSeriesPaint(0, colors[index]);
        }
    }

    private XYDataset createDataset() {

        XYSeriesCollection dataSet = new XYSeriesCollection();

        for (PrecisionRecallCurve curve : curveList) {
            System.out.println(nameQueue.remove());
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
            System.out.println(curve.getName());
            for (String id : pointId) {
                double p = curve.get(id + "_Precision");

                double r = curve.get(id + "_Recall");

                double fmeasure = 2.0 * (p * r) / (p + r);

                series.add(r, fmeasure);
                if (r > gap) {
                    System.out.println("F-measure: " + p + " @Recall: " + r);
                    gap += 0.1;
                }
            }
            dataSet.addSeries(series);
        }
        return dataSet;
    }

    public void addLine(Result result) {
        resultsInCurve.add(result);
        curveList.add(result.getPrecisionRecallCurve());
        labelMap.put(result.getPrecisionRecallCurve(), result.getModel() + " " + result.getAlgorithmName());
        nameQueue.add(result.getAlgorithmName());
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
