package view;

import raster.RasterBufferedImage;

import javax.swing.*;
import java.awt.*;

// JPanel ktery vykresluje nas RasterBufferedImage
public class Panel extends JPanel {
    private final RasterBufferedImage raster;

    public Panel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        raster = new RasterBufferedImage(width, height);
        raster.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        raster.repaint(g);
    }

    public RasterBufferedImage getRaster() {
        return raster;
    }
}
