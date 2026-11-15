package controller;

import clip.CohenSutherland;
import clip.SutherlandHodgman;
import fill.CheckerPattern;
import fill.PatternFill;
import fill.ScanLine;
import fill.SeedFill;
import model.Line;
import model.Point;
import model.Polygon;
import raster.RasterBufferedImage;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerBresenham;
import rasterize.PolygonRasterizer;
import view.Panel;
import view.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// Hlavni controller pro ovladani mysi, klavesnice a horni GUI listy
public class Controller2D {
    private final Window window;
    private final Panel panel;
    private final RasterBufferedImage raster;
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;

    // barvy pro ruzne prvky ve scene (nektere lze menit pres GUI tlacitka - Bonus 2)
    private static final int COLOR_LINE          = 0x00E5FF; // azurova
    private static final int COLOR_ELASTIC       = 0xFF9100; // oranzova pruzna cara
    private static final int COLOR_CLIP_WINDOW   = 0xFF1744; // cervene orezavaci okno / polygon
    private static final int COLOR_VERTEX_HANDLE = 0xFFFFFF; // bile ctverecky vrcholu

    private int polygonEdgeColor = 0xFFFF00; // zluta hrana polygonu (lze zmenit v GUI)
    private int fillColor        = 0x00E676; // zelena barva vyplne / orezani (lze zmenit v GUI)
    private int seedBgColor      = 0xD500F9; // fialova (SeedFill pozadi)

    // ulozene objekty ve scene
    private final List<Line> lines = new ArrayList<>();
    private final Polygon userPolygon = new Polygon();
    private final Polygon clipPolygon = new Polygon(); // orezavaci konvexni polygon (min 5 vrcholu)

    // souradnice obdelnikoveho orezavaciho okna pro Cohen-Sutherland
    private int rectXMin = 220, rectYMin = 150, rectXMax = 680, rectYMax = 480;

    // pomocne promenne pro mys a klavesnici
    private Point startPoint = null;
    private Point currentMousePoint = null;
    private boolean shiftDown = false;

    // zapnuti / vypnuti jednotlivych algoritmu
    private boolean scanLineActive = false;
    private boolean polygonClipActive = false;
    private boolean lineClipActive = false;
    private boolean patternActive = false; // bonus 1: vyplneni vzorem (PatternFill)
    private SutherlandHodgman.OrientationMode orientationMode = SutherlandHodgman.OrientationMode.AUTO;

    // promenne pro editaci nejblizsiho vrcholu (bonus)
    private Line draggedLine = null;
    private boolean draggingFirstPoint = false;
    private Polygon draggedPolygon = null;
    private int draggedVertexIndex = -1;

    private enum Mode {
        LINE("1: Kreslení úsečky (LMB tažení, Shift = 45°/90°, RMB = přesun vrcholu)"),
        POLYGON("2: Kreslení polygonu (LMB = nový vrchol, RMB tažení = přesun, MMB = smazat vrchol)"),
        SEED_FILL_BG("3: Seed Fill - omezení BARVOU POZADÍ (klikni LMB do plochy)"),
        SEED_FILL_BOUNDARY("4: Seed Fill - omezení BARVOU HRANICE (klikni LMB do polygonu)"),
        EDIT_CLIP_RECT("5: Editace ořezávacího OKNA (LMB tažením nakresli nové obdélníkové okno)"),
        EDIT_CLIP_POLY("6: Editace ořezávacího POLYGONU (LMB = přidat vrchol, RMB = přesun vrcholu, R = reset 5-úhelníku)");

        final String description;
        Mode(String description) { this.description = description; }
    }

    private Mode currentMode = Mode.LINE;

    public Controller2D(Window window) {
        this.window = window;
        this.panel = window.getPanel();
        this.raster = panel.getRaster();
        this.lineRasterizer = new LineRasterizerBresenham(raster);
        this.polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        initDefaultClipPolygon();
        initToolBar();
        initListeners();
        redrawScene();
    }

    // Bonus 2: Horni GUI lista s tlacitky pro zmenu barvy vyplne, barvy okraje a prepinani funkci
    private void initToolBar() {
        JToolBar tb = window.getToolBar();

        JButton btnFillColor = new JButton("Barva výplně");
        btnFillColor.setFocusable(false);
        btnFillColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(window, "Vyber barvu výplně", new Color(fillColor));
            if (c != null) {
                fillColor = c.getRGB() & 0xFFFFFF;
                seedBgColor = fillColor;
                redrawScene();
            }
            panel.requestFocusInWindow();
        });

        JButton btnEdgeColor = new JButton("Barva hranice");
        btnEdgeColor.setFocusable(false);
        btnEdgeColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(window, "Vyber barvu hranice polygonu", new Color(polygonEdgeColor));
            if (c != null) {
                polygonEdgeColor = c.getRGB() & 0xFFFFFF;
                redrawScene();
            }
            panel.requestFocusInWindow();
        });

        JButton btnScanLine = new JButton("Scan-Line (S)");
        btnScanLine.setFocusable(false);
        btnScanLine.addActionListener(e -> {
            scanLineActive = !scanLineActive;
            redrawScene();
            panel.requestFocusInWindow();
        });

        JButton btnPattern = new JButton("Vzor PatternFill (V)");
        btnPattern.setFocusable(false);
        btnPattern.addActionListener(e -> {
            patternActive = !patternActive;
            redrawScene();
            panel.requestFocusInWindow();
        });

        JButton btnClipPoly = new JButton("Ořez polygonu (P)");
        btnClipPoly.setFocusable(false);
        btnClipPoly.addActionListener(e -> {
            polygonClipActive = !polygonClipActive;
            redrawScene();
            panel.requestFocusInWindow();
        });

        JButton btnClipLine = new JButton("Cohen-Sutherland (L)");
        btnClipLine.setFocusable(false);
        btnClipLine.addActionListener(e -> {
            lineClipActive = !lineClipActive;
            redrawScene();
            panel.requestFocusInWindow();
        });

        JButton btnClear = new JButton("Vymazat vše (C)");
        btnClear.setFocusable(false);
        btnClear.addActionListener(e -> {
            clearAll();
            panel.requestFocusInWindow();
        });

        tb.add(btnFillColor);
        tb.add(btnEdgeColor);
        tb.addSeparator();
        tb.add(btnScanLine);
        tb.add(btnPattern);
        tb.add(btnClipPoly);
        tb.add(btnClipLine);
        tb.addSeparator();
        tb.add(btnClear);
    }

    // vychozi konvexni 5-uhelnik pro orezavani
    private void initDefaultClipPolygon() {
        clipPolygon.clear();
        clipPolygon.addPoint(new Point(450, 120));
        clipPolygon.addPoint(new Point(680, 240));
        clipPolygon.addPoint(new Point(600, 500));
        clipPolygon.addPoint(new Point(300, 500));
        clipPolygon.addPoint(new Point(220, 240));
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
                shiftDown = e.isShiftDown();
                Point clicked = new Point(e.getX(), e.getY());

                // Prave tlacitko mysi (RMB) -> chyceni a presun existujiciho vrcholu (Bonus)
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClickPress(clicked);
                    return;
                }

                // Prostredni tlacitko mysi (MMB) -> smazani nejblizsiho vrcholu polygonu (Bonus)
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    int idx = userPolygon.findNearestPointIndex(clicked, 15);
                    if (idx != -1) {
                        userPolygon.removePoint(idx);
                        redrawScene();
                    }
                    return;
                }

                // Leve tlacitko mysi (LMB) podle aktivniho rezimu
                if (SwingUtilities.isLeftMouseButton(e)) {
                    PatternFill activePattern = patternActive ? new CheckerPattern(fillColor, 0x222222) : null;
                    switch (currentMode) {
                        case LINE:
                        case EDIT_CLIP_RECT:
                            startPoint = clicked;
                            currentMousePoint = clicked;
                            break;
                        case POLYGON:
                        case EDIT_CLIP_POLY:
                            currentMousePoint = clicked;
                            redrawScene();
                            break;
                        case SEED_FILL_BG:
                            int bgColor = raster.getPixel(clicked.getX(), clicked.getY());
                            new SeedFill(raster, clicked.getX(), clicked.getY(), seedBgColor, bgColor, false, activePattern).fill();
                            drawLegendOnly();
                            break;
                        case SEED_FILL_BOUNDARY:
                            new SeedFill(raster, clicked.getX(), clicked.getY(), fillColor, polygonEdgeColor, true, activePattern).fill();
                            drawLegendOnly();
                            break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                shiftDown = e.isShiftDown();
                Point released = new Point(e.getX(), e.getY());

                if (SwingUtilities.isRightMouseButton(e)) {
                    draggedLine = null;
                    draggedPolygon = null;
                    draggedVertexIndex = -1;
                    redrawScene();
                    return;
                }

                if (!SwingUtilities.isLeftMouseButton(e)) return;

                if (currentMode == Mode.LINE && startPoint != null) {
                    Point endPoint = shiftDown ? Line.getSnappedPoint(startPoint, released) : released;
                    lines.add(new Line(startPoint, endPoint, COLOR_LINE));
                    startPoint = null;
                    currentMousePoint = null;
                    redrawScene();
                } else if (currentMode == Mode.EDIT_CLIP_RECT && startPoint != null) {
                    updateClipRect(startPoint, released);
                    startPoint = null;
                    currentMousePoint = null;
                    lineClipActive = true;
                    redrawScene();
                } else if (currentMode == Mode.POLYGON) {
                    userPolygon.addPoint(released);
                    currentMousePoint = null;
                    redrawScene();
                } else if (currentMode == Mode.EDIT_CLIP_POLY) {
                    clipPolygon.addPoint(released);
                    currentMousePoint = null;
                    polygonClipActive = true;
                    redrawScene();
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                shiftDown = e.isShiftDown();
                currentMousePoint = new Point(e.getX(), e.getY());

                // Pokud drzime prave tlacitko, posouvame chyceny vrchol
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (draggedLine != null) {
                        if (draggingFirstPoint) draggedLine.setP1(currentMousePoint);
                        else draggedLine.setP2(currentMousePoint);
                        redrawScene();
                    } else if (draggedPolygon != null && draggedVertexIndex != -1) {
                        draggedPolygon.setPoint(draggedVertexIndex, currentMousePoint);
                        redrawScene();
                    }
                    return;
                }

                if (currentMode == Mode.EDIT_CLIP_RECT && startPoint != null) {
                    updateClipRect(startPoint, currentMousePoint);
                }
                if (currentMode != Mode.SEED_FILL_BG && currentMode != Mode.SEED_FILL_BOUNDARY) {
                    redrawScene();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMousePoint = new Point(e.getX(), e.getY());
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                shiftDown = e.isShiftDown();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1:
                        currentMode = Mode.LINE;
                        redrawScene();
                        break;
                    case KeyEvent.VK_2:
                        currentMode = Mode.POLYGON;
                        redrawScene();
                        break;
                    case KeyEvent.VK_3:
                        currentMode = Mode.SEED_FILL_BG;
                        drawLegendOnly();
                        break;
                    case KeyEvent.VK_4:
                        currentMode = Mode.SEED_FILL_BOUNDARY;
                        drawLegendOnly();
                        break;
                    case KeyEvent.VK_5:
                        currentMode = Mode.EDIT_CLIP_RECT;
                        lineClipActive = true;
                        redrawScene();
                        break;
                    case KeyEvent.VK_6:
                        currentMode = Mode.EDIT_CLIP_POLY;
                        polygonClipActive = true;
                        redrawScene();
                        break;
                    case KeyEvent.VK_S:
                        scanLineActive = !scanLineActive;
                        redrawScene();
                        break;
                    case KeyEvent.VK_P:
                        polygonClipActive = !polygonClipActive;
                        redrawScene();
                        break;
                    case KeyEvent.VK_L:
                        lineClipActive = !lineClipActive;
                        redrawScene();
                        break;
                    case KeyEvent.VK_V:
                        // Bonus 1: prepinani vyplnovani vzorem (PatternFill)
                        patternActive = !patternActive;
                        redrawScene();
                        break;
                    case KeyEvent.VK_O:
                        // Prepinani orientace orezavaciho polygonu (AUTO -> CW -> CCW)
                        SutherlandHodgman.OrientationMode[] modes = SutherlandHodgman.OrientationMode.values();
                        orientationMode = modes[(orientationMode.ordinal() + 1) % modes.length];
                        redrawScene();
                        break;
                    case KeyEvent.VK_I:
                        // Bonus: vlozeni vrcholu na nejblizsi hranu polygonu v miste kurzoru
                        if (currentMousePoint != null) {
                            userPolygon.insertPointOnNearestEdge(currentMousePoint);
                            redrawScene();
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        // Bonus: smazani celeho polygonu
                        userPolygon.clear();
                        redrawScene();
                        break;
                    case KeyEvent.VK_R:
                        // Reset orezavaciho polygonu nebo vycisteni pri zadavani noveho
                        if (currentMode == Mode.EDIT_CLIP_POLY) {
                            clipPolygon.clear();
                        } else {
                            initDefaultClipPolygon();
                        }
                        redrawScene();
                        break;
                    case KeyEvent.VK_C:
                        clearAll();
                        break;
                }
                // Okamzite prekresleni pruzne cary pri stisku klavesy Shift behem tazeni mysi
                if (currentMode == Mode.LINE && startPoint != null) {
                    redrawScene();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                shiftDown = e.isShiftDown();
                if (currentMode == Mode.LINE && startPoint != null) {
                    redrawScene();
                }
            }
        });
    }

    // vyhledani nejblizsiho vrcholu usecky nebo polygonu pri kliknuti pravym tlacitkem
    private void handleRightClickPress(Point clicked) {
        double maxDist = 15.0;

        if (currentMode == Mode.EDIT_CLIP_POLY) {
            int idx = clipPolygon.findNearestPointIndex(clicked, maxDist);
            if (idx != -1) {
                draggedPolygon = clipPolygon;
                draggedVertexIndex = idx;
            }
            return;
        }

        // nejdriv zkusime chytit vrchol uzivatelskeho polygonu
        int polyIdx = userPolygon.findNearestPointIndex(clicked, maxDist);
        if (polyIdx != -1) {
            draggedPolygon = userPolygon;
            draggedVertexIndex = polyIdx;
            return;
        }

        // jinak zkusime chytit koncovy bod nektere usecky
        for (Line line : lines) {
            if (line.getP1().distanceTo(clicked) < maxDist) {
                draggedLine = line;
                draggingFirstPoint = true;
                return;
            }
            if (line.getP2().distanceTo(clicked) < maxDist) {
                draggedLine = line;
                draggingFirstPoint = false;
                return;
            }
        }
    }

    private void updateClipRect(Point p1, Point p2) {
        rectXMin = Math.min(p1.getX(), p2.getX());
        rectYMin = Math.min(p1.getY(), p2.getY());
        rectXMax = Math.max(p1.getX(), p2.getX());
        rectYMax = Math.max(p1.getY(), p2.getY());
    }

    // klavesa C - vymazani platna a vsech struktur
    private void clearAll() {
        lines.clear();
        userPolygon.clear();
        initDefaultClipPolygon();
        rectXMin = 220; rectYMin = 150; rectXMax = 680; rectYMax = 480;
        startPoint = null;
        currentMousePoint = null;
        scanLineActive = false;
        polygonClipActive = false;
        lineClipActive = false;
        patternActive = false;
        orientationMode = SutherlandHodgman.OrientationMode.AUTO;
        currentMode = Mode.LINE;
        redrawScene();
    }

    // kompletni prekresleni vsech objektu na platno
    private void redrawScene() {
        raster.clear();

        // 1. Vykresleni orezavaciho obdelnikoveho okna (Cohen-Sutherland)
        if (lineClipActive || currentMode == Mode.EDIT_CLIP_RECT) {
            drawRectWindow(rectXMin, rectYMin, rectXMax, rectYMax, COLOR_CLIP_WINDOW);
        }

        // 2. Vykresleni orezavaciho konvexniho polygonu (Sutherland-Hodgman)
        if (polygonClipActive || currentMode == Mode.EDIT_CLIP_POLY) {
            polygonRasterizer.rasterize(clipPolygon, COLOR_CLIP_WINDOW);
            drawPolygonHandles(clipPolygon, COLOR_CLIP_WINDOW);
        }

        // 3. Vykresleni a pripadne orezani usecek
        for (Line line : lines) {
            if (lineClipActive) {
                lineRasterizer.rasterize(new Line(line.getP1(), line.getP2(), 0x444444));
                Line clipped = CohenSutherland.clip(line, rectXMin, rectYMin, rectXMax, rectYMax, fillColor);
                if (clipped != null) {
                    lineRasterizer.rasterize(clipped);
                }
            } else {
                lineRasterizer.rasterize(line);
            }
            drawHandle(line.getP1(), COLOR_VERTEX_HANDLE);
            drawHandle(line.getP2(), COLOR_VERTEX_HANDLE);
        }

        // 4. Vykresleni, orezani a Scan-Line vyplneni polygonu
        Polygon targetPolygon = userPolygon;
        if (polygonClipActive && userPolygon.getCount() >= 3 && clipPolygon.getCount() >= 3) {
            polygonRasterizer.rasterize(userPolygon, 0x555555);
            targetPolygon = SutherlandHodgman.clip(userPolygon, clipPolygon, orientationMode);
        }

        PatternFill activePattern = patternActive ? new CheckerPattern(fillColor, 0x1A472A) : null;
        if (scanLineActive && targetPolygon.getCount() >= 3) {
            new ScanLine(raster, lineRasterizer, targetPolygon, fillColor, polygonEdgeColor, activePattern).fill();
        } else {
            polygonRasterizer.rasterize(targetPolygon, polygonClipActive ? fillColor : polygonEdgeColor);
        }
        drawPolygonHandles(userPolygon, COLOR_VERTEX_HANDLE);

        // 5. Pruzna cara pro usecku
        if (currentMode == Mode.LINE && startPoint != null && currentMousePoint != null) {
            Point end = shiftDown ? Line.getSnappedPoint(startPoint, currentMousePoint) : currentMousePoint;
            lineRasterizer.rasterize(startPoint, end, COLOR_ELASTIC);
        }

        // 6. Pruzna cara pro uzivatelsky polygon (k prvnimu a poslednimu vrcholu)
        if (currentMode == Mode.POLYGON && currentMousePoint != null && userPolygon.getCount() > 0) {
            Point first = userPolygon.getPoint(0);
            Point last = userPolygon.getPoint(userPolygon.getCount() - 1);
            lineRasterizer.rasterize(last, currentMousePoint, COLOR_ELASTIC);
            if (userPolygon.getCount() >= 2) {
                lineRasterizer.rasterize(first, currentMousePoint, COLOR_ELASTIC);
            }
        }

        // 7. Pruzna cara pri zadavani orezavaciho polygonu
        if (currentMode == Mode.EDIT_CLIP_POLY && currentMousePoint != null && clipPolygon.getCount() > 0) {
            Point first = clipPolygon.getPoint(0);
            Point last = clipPolygon.getPoint(clipPolygon.getCount() - 1);
            lineRasterizer.rasterize(last, currentMousePoint, COLOR_ELASTIC);
            if (clipPolygon.getCount() >= 2) {
                lineRasterizer.rasterize(first, currentMousePoint, COLOR_ELASTIC);
            }
        }

        drawHUD();
        panel.repaint();
    }

    private void drawRectWindow(int xMin, int yMin, int xMax, int yMax, int color) {
        lineRasterizer.rasterize(new Point(xMin, yMin), new Point(xMax, yMin), color);
        lineRasterizer.rasterize(new Point(xMax, yMin), new Point(xMax, yMax), color);
        lineRasterizer.rasterize(new Point(xMax, yMax), new Point(xMin, yMax), color);
        lineRasterizer.rasterize(new Point(xMin, yMax), new Point(xMin, yMin), color);
    }

    // vykresli maly ctverecek (3x3 px) kolem vrcholu pro snazsi chyceni mysi
    private void drawHandle(Point p, int color) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                raster.setPixel(p.getX() + dx, p.getY() + dy, color);
            }
        }
    }

    private void drawPolygonHandles(Polygon poly, int color) {
        for (int i = 0; i < poly.getCount(); i++) {
            drawHandle(poly.getPoint(i), color);
        }
    }

    private void drawLegendOnly() {
        Graphics2D g = (Graphics2D) raster.getImg().getGraphics();
        g.setColor(new Color(0x1E1E1E));
        g.fillRect(0, 0, raster.getWidth(), 105);
        g.dispose();
        drawHUD();
        panel.repaint();
    }

    // textovy prehled ovladani v horni casti okna
    private void drawHUD() {
        Graphics2D g = (Graphics2D) raster.getImg().getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(Color.WHITE);

        g.drawString("Režim: " + currentMode.description, 15, 22);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(0xCCCCCC));
        g.drawString("Režimy: [1] Úsečka (+Shift) | [2] Polygon | [3] SeedFill (Pozadí) | [4] SeedFill (Hranice) | [5] Edit Okna | [6] Edit Ořez.Poly (R=smazat)", 15, 44);
        g.drawString(String.format("Algoritmy: [S] ScanLine (%s) | [P] Ořez.Polygonu (%s) | [L] Cohen-Sutherland (%s) | [V] Vzor (%s) | [O] Orientace: %s",
                scanLineActive ? "ZAP" : "vyp",
                polygonClipActive ? "ZAP" : "vyp",
                lineClipActive ? "ZAP" : "vyp",
                patternActive ? "ZAP" : "vyp",
                orientationMode.label), 15, 64);
        g.drawString("Bonusy & Akce: [RMB tažení] Přesun vrcholu | [MMB klik] Smazat vrchol | [I] Vložit vrchol na hranu | [Del] Smazat polygon | [C] Vymazat vše", 15, 84);
        g.dispose();
    }
}
