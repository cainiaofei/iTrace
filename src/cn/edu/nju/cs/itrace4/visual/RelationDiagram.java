package cn.edu.nju.cs.itrace4.visual;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.metrics.PrecisionCompare;
import cn.edu.nju.cs.itrace4.core.metrics.PrecisionPosition;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.jfreechart.CombinedCategoryPlot;
import javafx.util.Pair;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by niejia on 15/4/29.
 */
public class RelationDiagram extends ApplicationFrame {

    PrecisionCompare precisionCompare;

    public RelationDiagram() {
        super("Relation Diagram");
    }

    public RelationDiagram(PrecisionCompare precisionCompare) {
        super("Relation Diagram");
        this.precisionCompare = precisionCompare;
    }

    public RelationDiagram(Result baseline, Result ours, SimilarityMatrix rtm) {
        super("Relation Diagram");
        PrecisionPosition precisionPosition1 = new PrecisionPosition(baseline.getMatrix(), rtm);

        PrecisionPosition precisionPosition2 = new PrecisionPosition(ours.getMatrix(), rtm);
        this.precisionCompare = new PrecisionCompare(precisionPosition1, precisionPosition2);
    }

    public void showDiagram() {
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.setVisible(true);
    }

    private JPanel createChartPanel() {
        CategoryDataset dataset = createDataset();

        NumberAxis rangeAxis1 = new NumberAxis("Value");
        rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
        renderer1.setBaseToolTipGenerator(
                new StandardCategoryToolTipGenerator());
        CategoryPlot subplot1 = new CategoryPlot(dataset, null, rangeAxis1,
                renderer1);
        subplot1.setDomainGridlinesVisible(true);
        renderer1.setSeriesVisibleInLegend(false);

        CategoryAxis domainAxis = new CategoryAxis("Relation Diagram");
        CombinedCategoryPlot plot = new CombinedCategoryPlot(
                domainAxis, new NumberAxis("Precision"));
        plot.add(subplot1, 1);

        JFreeChart chart = new JFreeChart(
                "",
                new Font("SansSerif", Font.BOLD, 12), plot, true);

        return new ChartPanel(chart);
    }

    private CategoryDataset createDataset() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String type1 = "IR-ONLY";
        String type2 = "UD-DACD";

        int i = 1;
        for (Pair<Double, Double> pair : precisionCompare.getPrecisionPair()) {
            dataset.addValue(pair.getKey(), String.valueOf(i), type1);
            dataset.addValue(pair.getValue(), String.valueOf(i), type2);
            i++;
        }


        return dataset;
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

    public void diagramStore(String path) throws IOException {
        String diagramName = "Relation Diagram";
        BufferedImage img = getScreenShot(
                this.getContentPane());
        ImageIO.write(img, "png", new File(path + "/" + diagramName + ".png"));
    }



    public static void main(String[] args) {
        String title = "Relation Diagram";
        RelationDiagram demo = new RelationDiagram();
        demo.showDiagram();
    }
}
