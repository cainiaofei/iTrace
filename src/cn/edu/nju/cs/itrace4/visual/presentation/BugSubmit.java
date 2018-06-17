package cn.edu.nju.cs.itrace4.visual.presentation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.exception.FileException;
 
public class BugSubmit{
	private String filePath = "data/exp/iTrust/uc_origin/UC15.txt";
	private FileProcess fileProcess = new FileProcessTool();
	
	JFrame jf = new JFrame("User");
    JPanel jp1, jp2,jp3;
    private JMenuBar menuBar;
   	private JMenu file,tool,view,help;
    public BugSubmit() {
        menuBar = new JMenuBar();
    	menuBar.setBounds(0, 0, 100, 20);
		file = new JMenu("文件");tool = new JMenu("工具");
		view = new JMenu("视图");help = new JMenu("帮助");
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		jf.setJMenuBar(menuBar);
		
		jp1 = new JPanel(new BorderLayout());
        JLabel bugLabel = new JLabel("问题描述");
        bugLabel.setFont(new Font(null,Font.BOLD,18));
        
        JTextArea bugTXT = new JTextArea();
        bugTXT.setEditable(true);
       
        jp1.add(bugLabel, BorderLayout.NORTH);
        jp1.add(new JLabel());
        jp1.add(new JScrollPane(bugTXT));
        
       // jp1.add(help);jp1.add(btn);
        
        jp2 = new JPanel(new BorderLayout());
        JLabel label = new JLabel("异常输出");
        label.setFont(new Font(null,Font.BOLD,18));
        
        JTextArea ta = new JTextArea();
        ta.setEditable(true);
        try {
			ta.setText(fileProcess.getFileConent(filePath));
		} catch (FileException | IOException e) {
			e.printStackTrace();
		}
        //JTextPane ta = new JTextPane();
        //ta.setBounds(160,100,200,200);
        jp2.add(label, BorderLayout.NORTH);
        jp2.add(new JLabel());
        jp2.add(new JScrollPane(ta));
//        jp2.add(label);
//        jp2.setLayout(new BorderLayout());
 //       jp2.add(ta);
        JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jp1, jp2);
        
        //up and down
        jp3 = new JPanel();
        JButton btn  = new JButton("提交");
        btn.setBounds(600, 400, 300, 300);
        jp3.add(btn);
        //jp3.setLayout(null);
        JSplitPane jsp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, jp3);
        
        
        jf.getContentPane().add(jsp1);;
        jf.setBounds(300, 200, 700, 500);
        jf.setVisible(true);
        jsp.setDividerLocation(0.6);// 在1/2处进行拆分
        jf.setLayout(null);
    }
 
    
    
    public static void main(String[] args) {
        new BugSubmit();
    }
}