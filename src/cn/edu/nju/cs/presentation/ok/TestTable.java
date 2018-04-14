package cn.edu.nju.cs.presentation.ok;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.presentation.demo.QueryResult.MyCellRenderer;
import cn.edu.nju.cs.presentation.result.UDCompute;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

public class TestTable {
	
	private int index = 1;
	private String filePath = "tempUC/UC18.txt";
	
	private UDCompute udCompute = new UDCompute();
	private ModelFactory modelFactory = new ModelFactoryImp();
	
	private JFrame jf;
	private JToolBar toolBar;
	private JMenuBar menuBar;
	private JMenu file,tool,view,help;
	private FileProcess fp = new FileProcessTool();
	private JProgressBar codeDepencyProgress = new JProgressBar(0,20000);
	
	private Set<String> validSet = new HashSet<String>();
	private Set<String> noValidSet = new HashSet<String>(); 
	private Set<String> skipSet = new HashSet<String>();
	
	private Set<Integer> validPostionSet = new HashSet<Integer>();
	private Set<Integer> noValidPostionSet = new HashSet<Integer>();
	private Set<Integer> skipPostionSet = new HashSet<Integer>();
    public static void main(String[] args) {
        new TestTable();
    }

    public TestTable() {
    	String[][] data = init();
    	final DefaultTableModel model = new DefaultTableModel(
                new Object[]{"class","score"}, 
                0
        );
    	
    	for(String[] arr:data) {
    		model.addRow(new Object[]{arr[0],arr[1]});
    	}
//        model.addRow(new Object[]{arr[0]});
//        model.addRow(new Object[]{"C"});
//        model.addRow(new Object[]{"E"});
//        model.addRow(new Object[]{"G"});

        //JTable table = new JTable(data,cols);
        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new MyCellRenderer());

        JScrollPane js = new JScrollPane(table); 
        //JTable table = new JTable(model);
        

        JFrame frame = new JFrame("Testing");
        frame.add(js);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String[][] display(Result irResult) {
    	String target = "UC18";
    	Map<String,Double> map = irResult.matrix.getLinksForSourceId(target);
    	LinksList allLinks = new LinksList();
    	for(String req:map.keySet()) {
    		allLinks.add(new SingleLink(target,req,map.get(req)));
    	}
    	return showIrResultDialog(allLinks);
	}
    
    private String[][] showIrResultDialog(LinksList allLinks) {
    	Collections.sort(allLinks,Collections.reverseOrder());
    	int index = 0;
    	for(SingleLink link:allLinks) {
    		String className = link.getTargetArtifactId();
    		if(validSet.contains(className)) {
    			validPostionSet.add(index);
    		}
    		else if(noValidSet.contains(className)) {
    			noValidPostionSet.add(index);
    		}
    		else if(skipSet.contains(className)) {
    			skipPostionSet.add(index);
    		}
    		index++;
    	}
    	
    	String[][] data = getDataFromLinks(allLinks);
    	
    	return data;
    }
    
    private String[][] getDataFromLinks(LinksList allLinks) {
		String[][] data = new String[allLinks.size()][2];
		int index = 0;
		for(SingleLink link:allLinks) {
			String[] record = new String[3];
			//record[0] = link.getSourceArtifactId();
			record[0] = link.getTargetArtifactId();
			record[1] = link.getScore()+"";
			data[index++] = record;
		}
		return data;
	}
    
    private String[][] init() {
    	Result udResult = null;
		//call data model
		String[] result = {"0","0","vsm"};
		//showIRConfigDialog(jf,jf);
		try {
			udResult = udCompute.udExecute(modelFactory.generate(result[2]),validSet,noValidSet);
			noValidSet.remove("auth.surveyResults_jsp");
			skipSet.add("auth.surveyResults_jsp");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return display(udResult);
		
	}

	public class MyCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (index == 1) {
                cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.red);

            } 
            else if(index==2) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.GREEN);

            }
            else if(index==3) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.BLUE);

            }
            else if(index==4) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.YELLOW);

            }
            else {
                cellComponent.setBackground(Color.white);
                cellComponent.setForeground(Color.black);
            }
            if (isSelected) {
                cellComponent.setForeground(table.getSelectionForeground());
                cellComponent.setBackground(table.getSelectionBackground());
            }
            index++;
            return cellComponent;

        }

    }

}