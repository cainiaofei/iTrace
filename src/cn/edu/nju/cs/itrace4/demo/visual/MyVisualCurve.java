package cn.edu.nju.cs.itrace4.demo.visual;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;

import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class MyVisualCurve extends VisualCurve{
	
	  public void showChart(String projectName) {
	        JPanel chartPanel = createChartPanel(projectName);
	        add(chartPanel, BorderLayout.CENTER);
	        setSize(900, 650);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setLocationRelativeTo(null);

	        this.setVisible(true);
	    }

	private JPanel createChartPanel(String projectName) {
		String chartTitle = "Precision-Recall Curve ("+ projectName +")";
        String xAxisLabel = "Recall";
        String yAxisLabel = "Precision";

        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        customizeChart(chart);

        return new ChartPanel(chart);
	}
}
