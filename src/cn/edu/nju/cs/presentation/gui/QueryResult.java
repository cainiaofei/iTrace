package cn.edu.nju.cs.presentation.gui;

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
import javax.swing.table.DefaultTableCellRenderer;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.presentation.result.UDCompute;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

public class QueryResult{
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
	
	public QueryResult() {
		jf = new JFrame("需求可追踪查询工具");
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		menuBar.setBounds(0, 0, 100, 20);
		file = new JMenu("文件");tool = new JMenu("工具");
		view = new JMenu("视图");help = new JMenu("帮助");
		
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		menuBar.add(toolBar);
		
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
		display(udResult);
	}


	/**
	 * @date 2018.3.15
	 * @description display the result of IR.
	 */
    private void display(Result irResult) {
    	String target = "UC18";
    	Map<String,Double> map = irResult.matrix.getLinksForSourceId(target);
    	LinksList allLinks = new LinksList();
    	for(String req:map.keySet()) {
    		allLinks.add(new SingleLink(target,req,map.get(req)));
    	}
    	showIrResultDialog(jf,jf,allLinks);
	}
    
    private void showIrResultDialog(Frame owner, Component parentComponent,LinksList allLinks) {
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
    	JMenuBar mb = new JMenuBar();
    	mb.add(this.file);mb.add(this.tool);
    	mb.add(this.view);mb.add(this.help);
    	
    	JPanel leftPanel = new JPanel(new BorderLayout());
    	
    	String[] irModels = new String[20];
    	for(int i = 0; i<irModels.length;i++) {
    		irModels[i] = "UC"+(i+1);
    	}
    	
    	JComboBox models = new JComboBox(irModels);
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
        //bugTXT.setSize(300, 300);
        
        leftPanel.setLayout(null);
        
//        leftPanel.add(description);
        leftPanel.add(models);
        JScrollPane bb = new JScrollPane(bugTXT);
        bb.setBounds(widthX,heightY,width,height);
        leftPanel.add(bb);
        leftPanel.setSize(300, 500);
        
        leftPanel.add(btn);
        
    	JPanel panel = new JPanel();
    	String[] cols = {"class","score"};
    	jf.setResizable(false);
		jf.setLocationRelativeTo(parentComponent);
		JTable jt = new JTable(data,cols);
		jt.setDefaultRenderer(Object.class, new MyCellRenderer());
		
		JScrollPane js = new JScrollPane(jt); 
		js.setBounds(300, 0, 360, 500);
		panel.add(js);
		jf.setJMenuBar(mb);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, js);
		jf.setContentPane(jsp);
		
		jf.setSize(680, 600);
		jf.setLayout(null);
		jf.setVisible(true);
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


	public void setOneRowBackgroundColor(JTable table, int rowIndex,  
            Color color) {  
        try {  
            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {  
  
                public Component getTableCellRendererComponent(JTable table,  
                        Object value, boolean isSelected, boolean hasFocus,  
                        int row, int column) { 
                	
                	if(validPostionSet.contains(rowIndex)) {
                		setBackground(Color.GREEN);  
                	}
                	else if(noValidPostionSet.contains(rowIndex)) {
                		setBackground(Color.RED);
                	}
                	else if(skipPostionSet.contains(rowIndex)) {
                		setBackground(Color.ORANGE);
                	}
                	else {
                		setBackground(Color.WHITE);  
                	}
                	
                	
//                    if (row == rowIndex) {  
//                        setBackground(color);  
//                       // setForeground(Color.WHITE);  
//                    }
//                    else {
//                    	setBackground(Color.WHITE);  
//                    }
                    return super.getTableCellRendererComponent(table, value,  
                            isSelected, hasFocus, row, column);  
                }  
            };  
            int columnCount = table.getColumnCount();  
            for (int i = 0; i < columnCount; i++) {  
                table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);  
            }  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }  
	
	/**
	 * @date 2018.3.16
	 * @description the progress of capture code dependency.
	 */
	public void getCodeDepProgress() {
		int i = 0;
		while(i<=20000) {
			codeDepencyProgress.setValue(i);
			i += 1;
			//System.out.println(i);
		}
		try {
			Thread.sleep(1500);//150000
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new QueryResult();
	}

}
