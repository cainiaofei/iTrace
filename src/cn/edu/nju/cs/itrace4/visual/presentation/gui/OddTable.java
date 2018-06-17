package cn.edu.nju.cs.itrace4.visual.presentation.gui;
import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.table.*;
  
public class OddTable extends JTable {
    public OddTable(int numRows, int numColumns) {
        super(numRows, numColumns);
    }
  
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
        Component comp = super.prepareRenderer(renderer, row, column);
        int value = ((Integer) getValueAt(row, column)).intValue();
        Color c = isCellSelected(row, column) ? Color.RED : Color.BLUE;
        comp.setBackground(value%2 ==0? c: c.darker());
        return comp;
    }
  
    public static void main(String[] args) {
        JTable t = new OddTable(25,1);
        Random rnd = new Random();
        for(int r = 0; r < t.getRowCount(); ++r)
            for(int c = 0; c < t.getColumnCount(); ++c)
                t.setValueAt(new Integer(rnd.nextInt(1000)), r, c);
        final JFrame f = new JFrame("CheckedTable");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new JScrollPane(t));
        f.pack();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }
}