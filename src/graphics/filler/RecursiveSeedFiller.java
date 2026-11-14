package graphics.filler;

import graphics.Raster;
import java.util.ArrayDeque;
import java.util.Deque;

public class RecursiveSeedFiller implements SeedFiller {

    private final Raster raster;

    public RecursiveSeedFiller(Raster raster) {
        this.raster = raster;
    }

    @Override
    public boolean fill(int x1, int y1, int targetColor, int fillColor) {
        if (targetColor == fillColor || raster.getPixel(x1, y1) == null || raster.getPixel(x1, y1) != targetColor) {
            return false;
        }

        fillRegion(x1, y1, targetColor, fillColor);
        return true;
    }

    private void fillRegion(int x, int y, int targetColor, int fillColor) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});
        while (!stack.isEmpty()) {
            int[] point = stack.pop();

            int px = point[0];
            int py = point[1];

            Integer color = raster.getPixel(px, py);

            if (color == null || color != targetColor) {
                continue;
            }

            raster.setPixel(px, py, fillColor);
            stack.push(new int[]{px - 1, py});
            stack.push(new int[]{px + 1, py});
            stack.push(new int[]{px, py - 1});
            stack.push(new int[]{px, py + 1});
        }

    }
    public boolean fillBoundary(int x, int y, int boundaryColor, int fillColor) {
        Integer color = raster.getPixel(x, y);

        if (color == null || color == boundaryColor || color == fillColor) {
            return false;
        }

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] point = stack.pop();

            int px = point[0];
            int py = point[1];

            color = raster.getPixel(px, py);

            if (color == null || color == boundaryColor || color == fillColor) {
                continue;
            }

            raster.setPixel(px, py, fillColor);

            stack.push(new int[]{px - 1, py});
            stack.push(new int[]{px + 1, py});
            stack.push(new int[]{px, py - 1});
            stack.push(new int[]{px, py + 1});
        }

        return true;
    }

}
