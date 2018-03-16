package cn.edu.nju.cs.presentation;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
 
public class DemoFrame{
	JFrame jf = new JFrame("用户判断");
    JPanel jp1, jp2;
    public DemoFrame() {
        jp1 = new JPanel();
        
        JLabel req = new JLabel("需求");
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
        help.setBounds(30, 320, 100, 50);
        
        JButton btn  = new JButton("提交");
        btn.setBounds(150, 320, 100, 50);
        
        jp1.add(req);jp1.add(code);jp1.add(reqValue);jp1.add(codeValue);
        jp1.add(relevant);jp1.add(unRelevant);
        jp1.add(skip); jp1.add(stop);
        
        jp1.add(help);jp1.add(btn);
        
        jp1.setLayout(null);
        
        jp2 = new JPanel(new BorderLayout());
        JLabel label = new JLabel("需求文本");
        
        JTextArea ta = new JTextArea();
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
        jf.setBounds(300, 200, 500, 500);
        jf.setVisible(true);
        jsp.setDividerLocation(0.6);// 在1/2处进行拆分
        jf.setLayout(null);
    }
 
    public static void main(String[] args) {
        new DemoFrame();
    }
}