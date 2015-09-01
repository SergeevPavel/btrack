package ui;

import ru.spbau.sergeev.btrack.client.Client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;
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
    private ProgressBar progressBar;
    private javax.swing.Timer updateTableTimer;

    public MainWindow(Client client) {
        super("btrack");
        log.log(Level.INFO, "Create main window");
        setContentPane(panel);
        initUI();
        addFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    onAddFile();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error on add file", ex);
                }
            }
        });
        stopSeedingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    onStopSeeding();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error on stop seeding", ex);
                }
            }
        });
        quitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    onQuit();
                    dispose();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error on quit", ex);
                }
            }
        });
        filesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    try {
                        onSelectedRowChanged();
                    } catch (Exception ex) {
                        log.log(Level.SEVERE, "Error on selected row changed", ex);
                    }
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onQuit();
                e.getWindow().dispose();
            }
        });
        int delay = 100;
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final int selectedRow = filesTable.getSelectedRow();
                ((FilesTableModel)filesTable.getModel()).fireTableDataChanged(); // periodic update Table
                if (selectedRow >= 0) {
                    filesTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
                progressBar.setSelectedRow(selectedRow);
                progressBar.repaint();
            }
        };
        updateTableTimer = new javax.swing.Timer(delay, taskPerformer);
        updateTableTimer.start();

//        setSize(600, 500);
        pack();
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initUI() {
        filesTable.setModel(new FilesTableModel());
        final FilesTableRender render = new FilesTableRender();
        for (int i = 0; i < 3; i++) {
            filesTable.getColumnModel().getColumn(i).setCellRenderer(render);
        }
        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void onAddFile() throws IOException {
        log.log(Level.INFO, "add file");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(Client.downloadPath.toFile());
        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            Path path = fc.getSelectedFile().toPath();
            log.log(Level.INFO, "Selected file " + path.toString());
            Client.client.addFile(path);
        }
    }

    private void onStopSeeding() {
        log.log(Level.INFO, "Stop seeding");
        final int rowIndex = filesTable.getSelectedRow();
        Client.client.stopSeeding(rowIndex);
        log.log(Level.INFO, String.format("Stop seeding row: %d", rowIndex));
    }

    private void onQuit() {
        log.log(Level.INFO, "Quit");
        updateTableTimer.stop();
        Client.client.shutDown();
    }

    private void onSelectedRowChanged() {
        //log.log(Level.INFO, "Selected: " + filesTable.getValueAt(filesTable.getSelectedRow(), 0).toString());
    }

}
