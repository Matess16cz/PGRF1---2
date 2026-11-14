package graphics.filler;

public interface SeedFiller {

    boolean fill(int x1, int y1, int targetColor, int fillColor);

    boolean fillBoundary(int x, int y, int boundaryColor, int fillColor);

}
