package ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author pavel
 */
public class FilesTableModel extends AbstractTableModel {
    private List<List<String>> data;
    private int columnCount = 3;
    private String[] columnNames = new String[] {"Filename", "Downloaded", "Available"};

    public FilesTableModel() {
        data = new ArrayList<>();
        data.add(Arrays.asList("book1.txt", "10%", "20%"));
        data.add(Arrays.asList("book2.txt", "10%", "20%"));
        data.add(Arrays.asList("book3.txt", "10%", "20%"));
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
