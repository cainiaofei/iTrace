package cn.edu.nju.cs.presentation.demo;
import java.awt.BorderLayout;
    import java.awt.Color;
    import java.awt.Font;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
    import javax.swing.text.BadLocationException;
    import javax.swing.text.Style;
    import javax.swing.text.StyleConstants;
    import javax.swing.text.StyleContext;

import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

    public class SQLEditor extends JFrame {
    	private String stopWordsList = "./data/exp/stopwords/stop-words_english_1_en.txt";
    	private String fileName = "tempUC/UC18.txt";
        private static final long serialVersionUID = 1L;
        private Set<String> keyWord = new HashSet<String>();
        private FileProcess fileProcess = new FileProcessTool();
        private JMenuBar menuBar = new JMenuBar();
        
        public void getKeyWordSet() {
        	Set<String> stopList = getStopWordList(stopWordsList);
        	String content = null;
			try {
				content = fileProcess.getFileConent(fileName);
			} catch (FileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	String[] words = content.split("\\(|\\)|\\s|\\.|;|@|\\<|\\>|\\{|\\}");
        	for(String word:words) {
        		if(!stopList.contains(word.toUpperCase()) && word.length()>1) {
        			keyWord.add(word.toUpperCase());
        		}
        	}
        }
        
        private Set<String> getStopWordList(String stopWordsPath) {
			Set<String> stopList = new HashSet<String>();
			String content = null;
			try {
				content = fileProcess.getFileConent(stopWordsPath);
			} catch (FileException | IOException e) {
				e.printStackTrace();
			}
			String[] words = content.split("\n");
			for(String word:words) {
				stopList.add(word.toUpperCase());
			}
			stopList.add("ITRUST");
			stopList.add("A");
			return stopList;
		}
        
        
		public SQLEditor() {
			//menuBar.add(Box.createHorizontalGlue());
			JLabel similarityLabel = new JLabel("相似度值：");
			menuBar.add(similarityLabel);
			JTextField similarityValue = new JTextField(15);
			similarityValue.setMaximumSize(similarityValue.getPreferredSize());
			menuBar.add(similarityValue);
			
			JLabel methodLabel = new JLabel("方法名：");
			menuBar.add(methodLabel);
			JTextField methodName = new JTextField(15);
			methodName.setMaximumSize(methodName.getPreferredSize());
			menuBar.add(methodName);
			
			JLabel relevantReq = new JLabel("相关需求：");
			JButton bt = new JButton("UC12");
			menuBar.add(relevantReq);menuBar.add(bt);
			JButton bt1 = new JButton("UC15");
			menuBar.add(bt1);
			
//			JLabel keyWordLabel = new JLabel("关键词：");
//			menuBar.add(keyWordLabel);
//			JTextField keyWordList = new JTextField();
//			menuBar.add(keyWordList);
			
        	getKeyWordSet();
            StyleContext styleContext = new StyleContext();
            Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
            Style cwStyle = styleContext.addStyle("ConstantWidth", null);
            StyleConstants.setForeground(cwStyle, Color.BLUE);
            StyleConstants.setBold(cwStyle, true);

            final JTextPane pane = new JTextPane(new KeywordStyledDocument(defaultStyle, cwStyle,keyWord));
            pane.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.setTitle("UpdateHospitalListAction");
            this.setJMenuBar(menuBar);
            JScrollPane scrollPane = new JScrollPane(pane);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(375, 400);      
        }

        public static void main(String[] args) throws BadLocationException {
            SQLEditor app = new SQLEditor();
            app.setVisible(true);
        }
    }