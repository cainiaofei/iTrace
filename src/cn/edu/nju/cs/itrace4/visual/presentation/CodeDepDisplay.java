package cn.edu.nju.cs.itrace4.visual.presentation;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.exp.input.ModelFactory;
import cn.edu.nju.cs.itrace4.exp.input.ModelFactoryImp;

public class CodeDepDisplay implements ActionListener{
	private String model;
	private String[] irModels = {"vsm","lsi","js"};
	private final JComboBox models = new JComboBox(irModels);
	
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
	
	private TopologyStructure TopologyStructure;
	
	private JProgressBar codeDepencyProgress = new JProgressBar(0,20000);
	
	public CodeDepDisplay() {
		jf = new JFrame("代码依赖拓扑图");
		fileChooser = new JFileChooser();
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		toolBar.add(models);
		models.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				model = String.valueOf(models.getSelectedItem());
			}
		});
		menuBar.setBounds(0, 0, 100, 20);
		file = new JMenu("File");tool = new JMenu("Tool");
		view = new JMenu("view");help = new JMenu("help");
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
		System.out.println("the progress of code dependency");
		try {
			TopologyStructure.showCodeDependencyGraph(jf);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CodeDepDisplay tool = new CodeDepDisplay();
	}

}
