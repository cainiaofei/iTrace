package cn.edu.nju.cs.presentation;

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

import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;
 
public class DemoFrame{
	private String filePath = "data/exp/iTrust/uc_origin/UC15.txt";
	private FileProcess fileProcess = new FileProcessTool();
	
	JFrame jf = new JFrame("用户判断");
    JPanel jp1, jp2;
    private JMenuBar menuBar;
   	private JMenu file,tool,view,help;
    public DemoFrame() {
        jp1 = new JPanel();
        
        menuBar = new JMenuBar();
    	menuBar.setBounds(0, 0, 100, 20);
    	
		file = new JMenu("文件");tool = new JMenu("工具");
		view = new JMenu("视图");help = new JMenu("帮助");
		
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		jf.setJMenuBar(menuBar);
        
        JLabel req = new JLabel("需求");
        //req.setFont(new Font(null,Font.BOLD,12));
        
        req.setBounds(30, 50, 40, 20);
        JTextField reqValue = new JTextField();
        reqValue.setText("uc15");
        reqValue.setBounds(60, 50, 40, 20);
        
        JLabel code = new JLabel("类");
        code.setBounds(110,50,20,20);
        JTextField codeValue = new JTextField();
        codeValue.setText("AuthDaoValidator");
        codeValue.setBounds(130, 50, 100, 20);
        
        JRadioButton relevant = new JRadioButton("相关");
        relevant.setBounds(100, 100, 100, 50);
        JRadioButton unRelevant = new JRadioButton("不相关");
        unRelevant.setBounds(100, 150, 100, 50);
        JRadioButton skip = new JRadioButton("跳过");
        skip.setBounds(100, 200, 100, 50);
        JRadioButton stop = new JRadioButton("停止判断");
        stop.setBounds(100, 250, 100, 50);
        
        JButton help = new JButton("帮助");
        help.setBounds(30, 350, 100, 50);
        
        JButton btn  = new JButton("提交");
        btn.setBounds(150, 350, 100, 50);
        
        jp1.add(req);jp1.add(code);jp1.add(reqValue);jp1.add(codeValue);
        jp1.add(relevant);jp1.add(unRelevant);
        jp1.add(skip); jp1.add(stop);
        
        jp1.add(help);jp1.add(btn);
        
        jp1.setLayout(null);
        
        jp2 = new JPanel(new BorderLayout());
        JLabel label = new JLabel("需求文本");
        label.setFont(new Font(null,Font.BOLD,18));
        
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
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
        jf.getContentPane().add(jsp);;
        jf.setBounds(300, 200, 700, 500);
        jf.setVisible(true);
        jsp.setDividerLocation(0.6);// 在1/2处进行拆分
        jf.setLayout(null);
    }
 
    
    
    public static void main(String[] args) {
        new DemoFrame();
    }
}