package ui;

import ru.spbau.sergeev.btrack.client.Client;

import javax.swing.*;
import java.awt.*;

/**
 * @author pavel
 */

public class ProgressBar extends JPanel {
    private int selectedRow = -1;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawProgressbar(g);
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    private void drawProgressbar(Graphics g) {
        if (selectedRow < 0) {
            g.setColor(Color.white);
            g.drawRect(0, 0, this.getWidth(), this.getHeight());
            return;
        }
        final boolean[] isPresent = Client.client.bookStorage.getIsPresent(selectedRow);
        final boolean[] isAvailable = Client.client.bookStorage.getIsAvailable(selectedRow);
        final int cellHeight = this.getHeight();
        final double cellWidth = ((double)this.getWidth()) / Client.chaptersCount;
        for (int i = 0; i < Client.chaptersCount; i++) {
            if (isPresent[i]) {
                g.setColor(Color.green);
            } else if (!isAvailable[i]) {
                g.setColor(Color.red);
            } else {
                g.setColor(Color.white);
            }
            int x1 = (int)(i * cellWidth);
            int x2 = (int)((i + 1) * cellWidth);
            g.fillRect(x1, 0, x2 - x1, cellHeight);
//            g.fillRect((int)(i * cellWidth) + 1, 1, (int)cellWidth - 1, cellHeight - 1);
        }
    }
}
