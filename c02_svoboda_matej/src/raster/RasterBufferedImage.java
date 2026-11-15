package raster;

import java.awt.*;
import java.awt.image.BufferedImage;

// implementace rastru pomoci BufferedImage ze cviceni
public class RasterBufferedImage implements Raster {
    private final BufferedImage img;
    private int clearColor = 0x1E1E1E; // tmave sede pozadi

    public RasterBufferedImage(int width, int height) {
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void repaint(Graphics graphics) {
        graphics.drawImage(img, 0, 0, null);
    }

    public BufferedImage getImg() {
        return img;
    }

    @Override
    public void clear() {
        Graphics g = img.getGraphics();
        g.setColor(new Color(clearColor));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }

    @Override
    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    @Override
    public int getWidth() {
        return img.getWidth();
    }

    @Override
    public int getHeight() {
        return img.getHeight();
    }

    @Override
    public int getPixel(int x, int y) {
        // kontrola abychom nesahali mimo platno
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return img.getRGB(x, y) & 0xFFFFFF;
        }
        return clearColor & 0xFFFFFF;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        // osetreni vykreslovani mimo platno
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            img.setRGB(x, y, color);
        }
    }
}
