package ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetSocketAddress;

public class LoginWindow extends JDialog {
    private JPanel contentPane;
    private JTextField port;
    private JTextField address;
    private JButton connectButton;
    private InetSocketAddress isa;

    public InetSocketAddress getIsa() {
        return isa;
    }

    public LoginWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(connectButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                parseInetSocketAddress();
            }
        });
        pack();
        setVisible(true);
    }

    private void parseInetSocketAddress() {
        try {
            final String hostname = address.getText();
            final int port = Integer.parseInt(this.port.getText());
            isa = new InetSocketAddress(hostname, port);
            setVisible(false);
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
