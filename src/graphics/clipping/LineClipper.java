package graphics.clipping;

import model.Line;

public interface LineClipper {

    Line clip(Line line, int xmin, int ymin, int xmax, int ymax);

}