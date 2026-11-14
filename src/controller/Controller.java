package controller;

import graphics.filler.ScanlineFiller;
import graphics.filler.RecursiveSeedFiller;
import graphics.filler.SeedFiller;
import graphics.rasterizer.PolygonOutlineRasterizer;
import graphics.rasterizer.PolygonRasterizer;
import graphics.rasterizer.XiaolinWuLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;
import view.gui.Toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.AbstractAction;

import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_C;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

public class Controller {

    private final Canvas canvas;
    private final Toolbar toolbar;

    private Line previewLine;
    private final Polygon polygon;
    private final PolygonRasterizer polygonRasterizer;

    private Point seedFillPoint;
    private boolean seedFillMode;
    private boolean boundaryFillMode = false;
    private final SeedFiller seedFiller;
    private final ScanlineFiller scanlineFiller;

    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private static final int PREVIEW_COLOR = Color.RED.getRGB();
    private static final int FILL_COLOR = new Color(0x58BFA9).getRGB();

    public Controller(Canvas canvas, Toolbar toolbar) {
        this.canvas = canvas;
        this.toolbar = toolbar;
        this.polygon = new Polygon();

        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());
        this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(new XiaolinWuLineRasterizer(canvas.getRaster()));
    }

    public void init() {
        toolbar.addClearListener(_ -> clearDrawing());
        toolbar.addSeedFillListener(_ -> toggleSeedFillMode());
        canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_C, 0), "clearCanvas");

        canvas.getActionMap().put("clearCanvas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearDrawing();
            }
        });

        canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_B, 0), "toogleBoundaryFill");
        canvas.getActionMap().put("toogleBoundaryFill", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                boundaryFillMode = !boundaryFillMode;
                System.out.println("Seed Fill režim: " + (boundaryFillMode ? "Barva hranice" : "Barva pozadí"));
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    if (seedFillMode) {
                        fillAt(event);
                    } else {
                        startLine(event);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON1 || previewLine == null) {
                    return;
                }

                polygon.addLine(new Line(previewLine.getPoint1(), getEndPoint(event), LINE_COLOR));
                previewLine = null;
                render();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (previewLine != null) {
                    previewLine = new Line(previewLine.getPoint1(), getEndPoint(event), PREVIEW_COLOR);
                    render();
                }
            }
        });
    }

    private void render() {
        canvas.clear();
        //scanlineFiller.fill(polygon, FILL_COLOR);
        polygonRasterizer.rasterize(polygon, previewLine);
        if (seedFillPoint != null) {
            if (boundaryFillMode) {
                seedFiller.fillBoundary(seedFillPoint.getX(), seedFillPoint.getY(), LINE_COLOR, FILL_COLOR);
            } else {
                seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), FILL_COLOR);
            }
        }
        canvas.repaint();
    }

    private void clearDrawing() {
        polygon.clear();
        previewLine = null;
        seedFillPoint = null;
        seedFillMode = false;
        boundaryFillMode = false;
        toolbar.setSeedFillMode(false);
        render();
    }

    private void toggleSeedFillMode() {
        seedFillMode = !seedFillMode;
        toolbar.setSeedFillMode(seedFillMode);
    }

    private void fillAt(MouseEvent event) {
        Point seed = getPoint(event);
        Integer seedColor = canvas.getRaster().getPixel(seed.getX(), seed.getY());
        if (seedFillPoint != null && seedColor != null && seedColor == FILL_COLOR) {
            seedFillPoint = null;
            render();
            return;
        }

        boolean filled;
        if (boundaryFillMode) {
            filled = seedFiller.fillBoundary(seed.getX(), seed.getY(), LINE_COLOR, FILL_COLOR);
        } else {
            filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        }
        if (filled) {
            seedFillPoint = seed;
            render();
        }
    }

    private Point getPoint(MouseEvent event) {

        return new Point(event.getX(), event.getY());
    }

    private Point getEndPoint(MouseEvent event) {
        Point cursor =  getPoint(event);

        if(!event.isShiftDown() || previewLine == null) {
            return cursor;
        }

        return alignPoint(previewLine.getPoint1(), cursor);
    }
    private Point alignPoint(Point start, Point cursor) {
        int x1 = start.getX(), y1 = start.getY();
        int dx = cursor.getX() - x1, dy = cursor.getY() - y1;

        int diag = (dx + dy) / 2;
        int anti = (dx - dy) / 2;

        Point[] candidates = {
                new Point(x1 + dx, y1),
                new Point(x1, y1 + dy),
                new Point(x1 + diag, y1 + diag),
                new Point(x1 + anti, y1 - anti)
        };

        Point best = candidates[0];
        long bestDist = Long.MAX_VALUE;

        for (Point c : candidates) {
            long ddx = c.getX() - cursor.getX();
            long ddy = c.getY() - cursor.getY();

            long dist = ddx * ddx + ddy * ddy;

            if (dist < bestDist) {
                bestDist = dist;
                best = c;
            }
        }

        return best;
    }

    private void startLine(MouseEvent event) {
        Point startPoint = polygon.getLastPoint();
        if (startPoint == null) {
            startPoint = getPoint(event);
        }
        previewLine = new Line(startPoint, startPoint, PREVIEW_COLOR);
        render();
    }

}
