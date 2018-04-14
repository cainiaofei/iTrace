package cn.edu.nju.cs.presentation.tmp;
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
	
	private int index = 0;
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
    	file = new JMenu("文件");tool = new JMenu("工具");
		view = new JMenu("视图");help = new JMenu("帮助");
    	jf = new JFrame("需求可追踪查询工具");
    	String[][] data = init();
    	final DefaultTableModel model = new DefaultTableModel(
                new Object[]{"class","score"}, 
                0
        );
    	
    	for(String[] arr:data) {
    		model.addRow(new Object[]{arr[0],arr[1]});
    	}

    	JMenuBar mb = new JMenuBar();
    	mb.add(this.file);mb.add(this.tool);
    	mb.add(this.view);mb.add(this.help);
    	
    	JPanel leftPanel = new JPanel(new BorderLayout());
    	String[] irModels = new String[20];
    	for(int i = 0; i<irModels.length;i++) {
    		irModels[i] = "UC"+(i+1);
    	}
    	JComboBox models = new JComboBox(irModels);
    	models.setSelectedIndex(17);
    	models.setBounds(80, 5, 100, 20);
    	JButton btn  = new JButton("搜索");
        btn.setBounds(200, 5, 80, 20);
        leftPanel.add(btn);
    	JTextArea bugTXT = new JTextArea();
    	String uc = null;
    	try {
			uc = fp.getFileConent(filePath);
		} catch (FileException | IOException e) {
			e.printStackTrace();
		}
    	bugTXT.setText(uc);
        bugTXT.setEditable(false);
        bugTXT.setFont(new Font(null,0,16));
        int widthX = 1,heightY = 30;
        int width = 299,height = 470;
        bugTXT.setLineWrap(true); 
        bugTXT.setBounds(widthX,heightY,width,height);
        leftPanel.setLayout(null);
        leftPanel.add(models);
        JScrollPane bb = new JScrollPane(bugTXT);
        bb.setBounds(widthX,heightY,width,height);
        leftPanel.add(bb);
        leftPanel.setSize(300, 500);
        leftPanel.add(btn);
        
    	
        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new MyCellRenderer());
        JScrollPane js = new JScrollPane(table); 
    	js.setBounds(300, 0, 360, 500);
    	JPanel panel = new JPanel();
		panel.add(js);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, js);
		jf.setJMenuBar(mb);
		jf.setContentPane(jsp);
		jf.setSize(680, 580);
		jf.setLayout(null);
		jf.setVisible(true);
		
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
    		if(validSet.contains(className)) {//1
    			validPostionSet.add(2*index);
    			validPostionSet.add(2*index+1);
    		}
    		else if(noValidSet.contains(className)) {//5
    			noValidPostionSet.add(2*index);
    			noValidPostionSet.add(2*index+1);
    		}
    		else if(skipSet.contains(className)) {//4
    			skipPostionSet.add(2*index);
    			skipPostionSet.add(2*index+1);
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
			validSet.clear();
			validSet.add("HospitalsDAO");
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

            if (noValidPostionSet.contains(index)) {
                cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.red);
            } 
            else if(validPostionSet.contains(index)) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.GREEN);

            }
            else if(skipPostionSet.contains(index)) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.ORANGE);

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