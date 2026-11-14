package graphics.clipping;

import model.Line;
import model.Point;

public class CohenSutherlandClipper implements LineClipper {
    private static final int INSIDE = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 4;
    private static final int TOP = 8;

    @Override
    public Line clip(Line line, int xmin, int ymin, int xmax, int ymax) {
        double x1 = line.getPoint1().getX();
        double y1 = line.getPoint1().getY();

        double x2 = line.getPoint2().getX();
        double y2 = line.getPoint2().getY();

        int code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        int code2 = computeOutCode(x2, y2, xmin, ymin, xmax, ymax);

        while (true) {

            if ((code1 | code2) == 0) {
                break;
            }

            if ((code1 & code2) != 0) {
                return null;
            }

            int codeOut = code1 != INSIDE ? code1 : code2;

            double x = 0;
            double y = 0;

            if ((codeOut & TOP) != 0) {
                x = x1 + (x2 - x1) * (ymin - y1) / (y2 - y1);
                y = ymin;
            } else if ((codeOut & BOTTOM) != 0) {
                x = x1 + (x2 - x1) * (ymax - y1) / (y2 - y1);
                y = ymax;
            } else if ((codeOut & RIGHT) != 0) {
                y = y1 + (y2 - y1) * (xmax - x1) / (x2 - x1);
                x = xmax;
            } else if ((codeOut & LEFT) != 0) {
                y = y1 + (y2 - y1) * (xmin - x1) / (x2 - x1);
                x = xmin;
            }

            if (codeOut == code1) {
                x1 = x;
                y1 = y;
                code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
            } else {
                x2 = x;
                y2 = y;
                code2 = computeOutCode(x2, y2, xmin, ymin, xmax, ymax);
            }
        }

        return new Line(
                new Point((int) Math.round(x1), (int) Math.round(y1)),
                new Point((int) Math.round(x2), (int) Math.round(y2)),
                line.getColor()
        );
    }

    private int computeOutCode(double x, double y, int xmin, int ymin, int xmax, int ymax) {

        int code = INSIDE;

        if (x < xmin) {
            code |= LEFT;
        } else if (x > xmax) {
            code |= RIGHT;
        }

        if (y < ymin) {
            code |= TOP;
        } else if (y > ymax) {
            code |= BOTTOM;
        }

        return code;
    }
}