package ui;

import javax.swing.*;
import javax.swing.table.TableColumn;
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
        initUI();
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initUI() {
        filesTable.addColumn(new TableColumn());
        filesTable.addColumn(new TableColumn());
    }

    private void onAddFile() {
        log.log(Level.INFO, "add file");
    }

    private void onStopSeeding() {
        log.log(Level.INFO, "Stop seeding");
    }

    private void onQuit() {
        log.log(Level.INFO, "Quit");
        dispose();
    }
}
