package cn.edu.nju.cs.presentation.gui;

import java.awt.Color;

public class MyCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
		private int index = 1;
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (index == 0) {
                cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.red);

            } 
            else if(index==1) {
            	cellComponent.setForeground(Color.black);
                cellComponent.setBackground(Color.GREEN);

            }
            else {
                cellComponent.setBackground(Color.white);
                cellComponent.setForeground(Color.black);
            }
            if (isSelected) {
                cellComponent.setForeground(table.getSelectionForeground());
                cellComponent.setBackground(table.getSelectionBackground());
            }
            index++;
            return cellComponent;

        }

    }

