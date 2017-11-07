package cn.edu.nju.cs.tool;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Paint;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class Demo {
	private JFrame frame;
	public Demo() {
		this.frame = new JFrame();
	}
	
	public void process() {
		SparseGraph<Integer,Integer> g = new SparseGraph<Integer,Integer>();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 1, 2);
		g.addEdge(2, 2,3);
		Layout<Integer,Integer> layout = new CircleLayout<Integer,Integer>(g);
		BasicVisualizationServer<Integer,Integer> vv = new BasicVisualizationServer<Integer,Integer>(layout);
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>(){
			@Override
			public String transform(Integer index) {
				if(index==1) {
					return "one";
				}
				else {
					return "other";
				}
			}
		});
		
		vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<Integer,Paint>(){
			@Override
			public Paint transform(Integer input) {
				if(input==2) {
					return Color.BLUE;
				}
				else {
					return Color.RED;
				}
			}
		});
		
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		
		//----------------------------------------------
		
	}
	
	public static void main(String[] args) {
		Demo demo = new Demo();
		demo.process();
	}
}
