package graphics.clipping;

import model.Polygon;
import model.Line;
import model.Point;

import java.util.List;
import java.util.ArrayList;

public class SutherlandHodgmanClipper implements PolygonClipper {

    @Override
    public Polygon clip(Polygon subject, Polygon boundary) {
        List<Line> edges = subject.getLines();
        if (edges.size() < 3) {
            return new Polygon();
        }

        List<Point> vertices = new ArrayList<>();
        for (Line edge : edges) {
            vertices.add(edge.getPoint1());
        }

        List<Point> boundaryVertices = new ArrayList<>();
        for (Line edge : boundary.getLines()) {
            boundaryVertices.add(edge.getPoint1());
        }

        if (boundaryVertices.size() < 3) {
            return new Polygon();
        }

        double orientation = signedArea(boundaryVertices);

        if (orientation == 0) {
            return new Polygon();
        }

        List<Point> output = new ArrayList<>(vertices);
        for (int i = 0; i < boundaryVertices.size(); i++) {
            Point a = boundaryVertices.get(i);
            Point b = boundaryVertices.get((i + 1) % boundaryVertices.size());

            if (output.isEmpty()) {
                break;
            }

            List<Point> input = new ArrayList<>(output);
            output.clear();
            for (int j = 0; j < input.size(); j++) {
                Point current = input.get(j);
                Point previous = input.get((j - 1 +  input.size()) % input.size());
                boolean currentInside = isInside(a, b, current, orientation);
                boolean previousInside = isInside(a, b, previous, orientation);
                if (previousInside && currentInside) {
                    output.add(current);
                } else if (previousInside && !currentInside) {
                    Point intersectionPoint = intersection(previous, current, a, b);
                    output.add(intersectionPoint);
                } else if (!previousInside && currentInside) {
                    Point intersectionPoint = intersection(previous, current, a, b);
                    output.add(intersectionPoint);
                    output.add(current);
                }
            }

        }

        Polygon result = new Polygon();

        if (output.size() < 3) {
            return result;
        }

        for (int i = 0; i < output.size(); i++) {
            Point a = output.get(i);
            Point b = output.get((i + 1) % output.size());

            result.addLine(new Line(a, b, edges.getFirst().getColor()));
        }

        return result;
    }

    private double cross(Point a, Point b, Point p) {
        return (double) (b.getX() - a.getX()) * (p.getY() - a.getY()) - (double) (b.getY() - a.getY()) * (p.getX() - a.getX());
    }

    private double signedArea(List<Point> vertices) {
        double area = 0;

        for (int i = 0; i < vertices.size(); i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % vertices.size());

            area += (double) a.getX() * b.getY()
                    - (double) b.getX() * a.getY();
        }

        return area / 2.0;
    }

    private boolean isInside(Point a, Point b, Point p, double orientation) {
        return cross(a, b, p) * orientation >= 0;
    }

    private Point intersection(Point previous, Point current, Point a, Point b) {
        double d1 = cross(a, b, previous);
        double d2 = cross(a, b, current);

        double t = d1 / (d1 - d2);

        int x = (int) Math.round(
                previous.getX() + t * (current.getX() - previous.getX())
        );

        int y = (int) Math.round(
                previous.getY() + t * (current.getY() - previous.getY())
        );

        return new Point(x, y);
    }


}