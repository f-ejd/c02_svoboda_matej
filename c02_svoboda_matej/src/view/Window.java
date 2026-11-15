package view;

import javax.swing.*;
import java.awt.*;

// hlavni okno aplikace s horni listou nastroju (Bonus 2)
public class Window extends JFrame {
    private final Panel panel;
    private final JToolBar toolBar;

    public Window(int width, int height) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("PGRF1 - Úloha 2 (Vyplňování a ořezávání)");
        setLayout(new BorderLayout());

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        panel = new Panel(width, height);
        add(panel, BorderLayout.CENTER);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    public Panel getPanel() {
        return panel;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }
}
