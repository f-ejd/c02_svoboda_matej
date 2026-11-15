package app;

import controller.Controller2D;
import view.Window;

import javax.swing.*;

// spousteci trida aplikace
public class AppStart {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window(950, 680);
            new Controller2D(window);
        });
    }
}
