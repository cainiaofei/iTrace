package cn.edu.nju.cs.presentation;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;

public class Tool implements ActionListener{
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
	public Tool() {
		jf = new JFrame("requirement_code_traceability_tool");
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
		file = new JMenu("File");
		tool = new JMenu("Tool");
		view = new JMenu("view");
		help = new JMenu("help");

		importReq = new JMenuItem("import requirement");
		importReq.addActionListener(this);;
		importCode = new JMenuItem("import code");
		importCode.addActionListener(this);

		codeDepdencyCapture = new JMenuItem("codeDependencyCapture");
		irMethod = new JMenuItem("ir method");
		irMethod.addActionListener(this);
		tool.add(codeDepdencyCapture);
		tool.add(irMethod);

		file.add(importReq);
		file.add(importCode);
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		menuBar.add(toolBar);
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
			String[] result = showIRConfigDialog(jf,jf);
			try {
				irResult = irCompute.irExecute(modelFactory.generate(result[2]));
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			display(irResult);
		}
	}

	/**
	 * @date 2018.3.15
	 * @description display the result of IR.
	 */
    private void display(Result irResult) {
    	LinksList allLinks = irResult.matrix.allLinks();
    	for(SingleLink link:allLinks) {
    		System.out.println(link.getSourceArtifactId());
    	}
    	showIrResultDialog(jf,jf);
	}

    
    private void showIrResultDialog(Frame owner, Component parentComponent) {
    	JDialog dialog = new JDialog(owner,"信息检索方法结果展示",true);
    	JPanel panel = new JPanel();
    	dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentComponent);
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

	public static void main(String[] args) {
		Tool tool = new Tool();

	}

}
