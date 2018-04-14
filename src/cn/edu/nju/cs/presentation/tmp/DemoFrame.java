package cn.edu.nju.cs.presentation.tmp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.ButtonGroup;
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
	
	JFrame jf = new JFrame("user feedback");
    JPanel jp1, jp2;
    private JMenuBar menuBar;
   	private JMenu file,tool,view,help;
    public DemoFrame() {
        jp1 = new JPanel();
        
        menuBar = new JMenuBar();
    	menuBar.setBounds(0, 0, 100, 20);
    	
		file = new JMenu("file");tool = new JMenu("tool");
		view = new JMenu("view");help = new JMenu("help");
		
		menuBar.add(file);menuBar.add(tool);
		menuBar.add(view);menuBar.add(help);
		jf.setJMenuBar(menuBar);
        
        JLabel req = new JLabel("req");
        //req.setFont(new Font(null,Font.BOLD,12));
        int offset = 50;
        req.setBounds(30+offset, 50, 40, 20);
        JTextField reqValue = new JTextField();
        reqValue.setText("UC15");
        reqValue.setBounds(60+offset, 50, 40, 20);
        
        JLabel code = new JLabel("class");
        code.setBounds(110+offset,50,20,20);
        JTextField codeValue = new JTextField();
        codeValue.setText("AuthDaoValidator");
        codeValue.setBounds(130+offset, 50, 100, 20);
        
        ButtonGroup radioGroup = new ButtonGroup();
        JRadioButton relevant = new JRadioButton("relevant");
       // relevant.setBackground(Color.GREEN);
        relevant.setBounds(100+offset, 100, 100, 50);
        JRadioButton unRelevant = new JRadioButton("no relevant");
       // unRelevant.setBackground(Color.RED);
        unRelevant.setBounds(100+offset, 150, 100, 50);
        JRadioButton skip = new JRadioButton("skip");
       // skip.setBackground(Color.BLUE);
        skip.setBounds(100+offset, 200, 100, 50);
        JRadioButton stop = new JRadioButton("stop judge");
       // stop.setBackground(Color.YELLOW);
        stop.setBounds(100+offset, 250, 100, 50);
        
        JButton help = new JButton("help");
        help.setBounds(30+offset, 350, 100, 50);
        
        JButton btn  = new JButton("submit");
        btn.setBounds(150+offset, 350, 100, 50);
        
        jp1.add(req);jp1.add(code);jp1.add(reqValue);jp1.add(codeValue);
        radioGroup.add(relevant);radioGroup.add(unRelevant);
        radioGroup.add(skip); radioGroup.add(stop);
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
        jf.getContentPane().add(jp1);;
        jf.setBounds(300, 200, 400, 500);
        jf.setVisible(true);
        jf.setLayout(null);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
    
    
    public static void main(String[] args) {
        new DemoFrame();
    }
}