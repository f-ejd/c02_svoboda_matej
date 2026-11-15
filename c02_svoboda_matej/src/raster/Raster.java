package raster;

// zakladni rozhrani pro praci s rastrem (platnem)
public interface Raster {
    void clear();
    void setClearColor(int color);
    int getWidth();
    int getHeight();
    int getPixel(int x, int y);
    void setPixel(int x, int y, int color);
}
