package cn.edu.nju.cs.itrace4.visual.presentation.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.visual.presentation.CodeDependencyDisplay;
import cn.edu.nju.cs.itrace4.visual.presentation.IRCompute;
import cn.edu.nju.cs.itrace4.visual.presentation.PDFGenerate;
import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;

public class QueryResult implements ActionListener{
	private String model;
	
	private IRCompute irCompute = new IRCompute();
	private ModelFactory modelFactory = new ModelFactoryImp();
	
	private JFrame jf;
	private JToolBar toolBar;
	private JMenuBar menuBar;
	private JMenu file,tool,view,help;
	private JMenuItem importReq,importCode,codeDepdencyCapture,irMethod;
	private JFileChooser fileChooser;
	private String reqPath, codePath; // IR
	private PDFGenerate pdfGenerate = new PDFGenerate();
	
	private CodeDependencyDisplay codeDependencyDisplay;
	
	private JProgressBar codeDepencyProgress = new JProgressBar(0,20000);
	
	public QueryResult() {
		jf = new JFrame("需求可追踪查询工具");
		fileChooser = new JFileChooser();
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		menuBar.setBounds(0, 0, 100, 20);
		file = new JMenu("文件");tool = new JMenu("工具");
		view = new JMenu("视图");help = new JMenu("帮助");
		importReq = new JMenuItem("import requirement");
		importReq.addActionListener(this);;
		importCode = new JMenuItem("import code");
		importCode.addActionListener(this);

		codeDepdencyCapture = new JMenuItem("codeDependencyCapture");
		irMethod = new JMenuItem("ir method");
		
		codeDepdencyCapture.addActionListener(this);
		irMethod.addActionListener(this);
		tool.add(codeDepdencyCapture);
		tool.add(irMethod);

		file.add(importReq);
		file.add(importCode);
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		menuBar.add(toolBar);
		
		codeDepencyProgress.setBounds(100, 200, 400, 60);
		codeDepencyProgress.setValue(0);
		codeDepencyProgress.setStringPainted(true);
		
		//jf.add(codeDepencyProgress);
		//jf.add(menuBar);
		jf.setJMenuBar(menuBar);
		jf.setSize(600, 600);
		jf.setLayout(null);
		jf.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==importReq || e.getSource()==importCode) {
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = fileChooser.showOpenDialog(jf);
			if(state==JFileChooser.APPROVE_OPTION) {
				File dir = fileChooser.getSelectedFile();
				String dirPath = dir.getAbsolutePath();
				if(e.getSource()==importReq) {
					reqPath = dirPath;
				}
				else {
					codePath = dirPath;
				}
			}
		}
		else if(e.getSource()==irMethod) {
			Result irResult = null;
			//call data model
			String[] result = {"0","0","vsm"};
			//showIRConfigDialog(jf,jf);
			try {
				irResult = irCompute.irExecute(modelFactory.generate(result[2]));
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			display(irResult);
		}
		else if(e.getSource()==codeDepdencyCapture) {
			//display progress of capture code dependency.
			System.out.println("the progress of code dependency");
			getCodeDepProgress();
			try {
				CodeDependencyDisplay.showCodeDependencyGraph(jf);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @date 2018.3.15
	 * @description display the result of IR.
	 */
    private void display(Result irResult) {
    	String target = "UC18";
//    	LinksList allLinks = irResult.matrix.allLinks();
    	Map<String,Double> map = irResult.matrix.getLinksForSourceId(target);
    	LinksList allLinks = new LinksList();
    	for(String req:map.keySet()) {
    		allLinks.add(new SingleLink(target,req,map.get(req)));
    	}
    	showIrResultDialog(jf,jf,allLinks);
	}
    
    private void showIrResultDialog(Frame owner, Component parentComponent,LinksList allLinks) {
    	Collections.sort(allLinks,Collections.reverseOrder());
    	String[][] data = getDataFromLinks(allLinks);
    	JMenuBar mb = new JMenuBar();
    	mb.add(this.file);mb.add(this.tool);
    	mb.add(this.view);mb.add(this.help);
    	JDialog dialog = new JDialog(owner,"需求可追踪查询工具",false);
    	
    	JPanel leftPanel = new JPanel(new BorderLayout());
    	
//    	JLabel description = new JLabel("需求：");
//    	description.setFont(new Font(null,Font.BOLD,18));
//    	description.setBounds(20, 0, 80, 20);
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
        bugTXT.setEditable(true);
        bugTXT.setFont(new Font(null,0,20));
        
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
        //leftPanel.add(btn,BorderLayout.SOUTH);
        //leftPanel.setLayout(null);
        
        leftPanel.add(btn);
        
    	JPanel panel = new JPanel();
    	String[] cols = {"class","score"};
    	dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentComponent);
		JTable jt = new JTable(data,cols);
		//jt.setBounds(50, 0, 500, 500);
		JScrollPane js = new JScrollPane(jt); 
		js.setBounds(300, 0, 360, 500);
		panel.add(js);
		//js.setLayout(null);
		//panel.setLayout(null);
		dialog.setJMenuBar(mb);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, js);
		
		//jsp.setLayout(null);
		
		dialog.setContentPane(jsp);
		
		dialog.setSize(680, 600);
		dialog.setLayout(null);
		dialog.setVisible(true);
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

	/**
     * @date 2018.3.15
     * @description used to ir parameter config. 
     */
	private String[] showIRConfigDialog(Frame owner, Component parentComponent) {
		String[] res = new String[3];
		JPanel panel = new JPanel();
		JDialog dialog = new JDialog(owner, "参数配置", true);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentComponent);

		JLabel callThresholdLabel = new JLabel("callThreshold");
		callThresholdLabel.setBounds(100, 100, 100, 20);
		JTextField callValue = new JTextField();
		callValue.setBounds(200, 100, 60, 20);
		JLabel dataThresholdLabel = new JLabel("dataThreshold");
		dataThresholdLabel.setBounds(100, 150, 100, 20);
		JTextField dataValue = new JTextField();
		dataValue.setBounds(200, 150, 60, 20);

		JLabel modelName = new JLabel("model");
		modelName.setBounds(150,200,100,20);

		String[] modelList = {"VSM","LSI","JS"};
		JComboBox<String> modelKinds = new JComboBox<String>(modelList);
		modelKinds.setBounds(200, 200, 60, 20);

		JButton btn = new JButton("确定");
		btn.setBounds(150, 250, 80, 20);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double callThreshold = Double.valueOf(callValue.getText());
				double dataThreshold = Double.valueOf(dataValue.getText());
				String model = modelKinds.getSelectedItem().toString();
				res[0] = callThreshold +"";
				res[1] = dataThreshold+"";
				res[2] = model;
				dialog.dispose();
			}
		});

		panel.add(callThresholdLabel);
		panel.add(dataThresholdLabel);
		panel.add(callValue);
		panel.add(dataValue);
		panel.add(modelName);
		panel.add(modelKinds);
		panel.add(btn);
		panel.setLayout(null);

		dialog.setSize(400, 400);
		dialog.setContentPane(panel);
		dialog.setVisible(true);
		return res;
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
		QueryResult tool = new QueryResult();
		
	}

}
