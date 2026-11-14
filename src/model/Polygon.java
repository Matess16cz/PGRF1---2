package model;

import java.util.ArrayList;
import java.util.List;

public final class Polygon {

    private final List<Line> lines;

    public Polygon() {
        lines = new ArrayList<>();
    }

    public void clear() {
        lines.clear();
    }

    public Point getLastPoint() {
        return lines.isEmpty() ? null : lines.getLast().getPoint2();
    }

    public List<Line> getLines() {
        return List.copyOf(lines);
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public void changeColor(int color) {
        List<Line> updatedLines = new ArrayList<>();

        for (Line line : getLines()) {
            updatedLines.add(new Line(
                    line.getPoint1(),
                    line.getPoint2(),
                    color
            ));
        }

        clear();

        for (Line line : updatedLines) {
            addLine(line);
        }
    }

}
