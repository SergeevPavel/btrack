package ui;

import ru.spbau.sergeev.btrack.client.Client;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * @author pavel
 */
public class FilesTableModel extends AbstractTableModel {
    private int columnCount = 3;
    private String[] columnNames = new String[] {"Filename", "Downloaded", "Available"};

    public FilesTableModel() {
    }

    @Override
    public int getRowCount() {
        return Client.client.bookStorage.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Client.client.bookStorage.get(rowIndex).getBookName();
            case 1:
                return String.format("%.0f%%", Client.client.bookStorage.get(rowIndex).getCompletedPercent());
            case 2:
                return String.format("%.0f%%", Client.client.bookStorage.get(rowIndex).getAvailablePercent());
            default:
                assert false;
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Color getRowColor(int row, boolean isSelected) {
        if (Client.client.bookStorage.get(row).isSeeding()) {
            if (isSelected) {
                return Color.gray;
            } else {
                return Color.white;
            }
        } else {
            if (isSelected) {
                return Color.pink;
            } else {
                return Color.red;
            }
        }
    }
}
