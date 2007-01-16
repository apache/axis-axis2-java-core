package org.apache.axis2.tools.idea;

import javax.swing.table.AbstractTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: keith
 * Date: 21/10/2006
 * Time: 14:27:40
 * To change this template use File | Settings | File Templates.
 */


public class PackageNameTableModel extends AbstractTableModel {
    Object [] [] tableData;


    public PackageNameTableModel(Object[][] tableData) {
        this.tableData = tableData;
    }

    public void setTableData(Object [][] tableData) {
        this.tableData = tableData;
    }

    public String getColumnName(int c)
   {
      return columnNames[c];
   }

   public Class getColumnClass(int c)
   {
      return tableData[0][c].getClass();
   }

   public int getColumnCount()
   {
      return tableData[0].length;
   }

   public int getRowCount()
   {
      return tableData.length;
   }

   public Object getValueAt(int r, int c)
   {
      return tableData[r][c];
   }

   public void setValueAt(Object obj, int r, int c)
   {
      tableData[r][c] = obj;
   }

   public boolean isCellEditable(int r, int c)
   {
      return  c == PACKAGENAME_COLUMN;

   }

   public static final int NAMESPACE_COLUMN = 0;
   public static final int PACKAGENAME_COLUMN = 1;



   private String[] columnNames =
   {
      "Namespace", "Custom Package Name"
   };
}

