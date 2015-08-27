package ui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class MainWindow extends JFrame {
    private static Logger log = Logger.getLogger(MainWindow.class.getName());

    private JPanel panel;
    private JTable filesTable;
    private JButton addFileButton;
    private JButton stopSeedingButton;
    private JButton quitButton;

    public MainWindow() {
        super("btrack");
        log.log(Level.INFO, "Create main window");
        setContentPane(panel);
        initUI();
        addFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onAddFile();
            }
        });
        stopSeedingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onStopSeeding();
            }
        });
        quitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onQuit();
            }
        });
        filesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    onSelectedRowChanged();
                }
            }
        });
//        setSize(600, 500);
        pack();
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        filesTable.addMouseListener(new MouseAdapter() {
        });
    }

    private void initUI() {
        filesTable.setModel(new FilesTableModel());
        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void onAddFile() {
        log.log(Level.INFO, "add file");
    }

    private void onStopSeeding() {
        log.log(Level.INFO, "Stop seeding");
        final int rowIndex = filesTable.getSelectedRow();
        log.log(Level.INFO, String.format("Selected row: %d", rowIndex));
    }

    private void onQuit() {
        log.log(Level.INFO, "Quit");
        dispose();
    }

    private void onSelectedRowChanged() {
        log.log(Level.INFO, "Selected: " + filesTable.getValueAt(filesTable.getSelectedRow(), 0).toString());
    }
}
