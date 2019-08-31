package de.codefor.leipzig.wahldaten.utils;

import org.geojson.LngLatAlt;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolygonMerger {
    public static int COORD_PRECISION = 10000000;

    public static List<LngLatAlt> getCoordsFromArea(Area area) {
        List<LngLatAlt> geoCoords = new ArrayList<>();
        PathIterator iter = area.getPathIterator(null);
        while (!iter.isDone()) {
            double[] coords = new double[6];
            int type = iter.currentSegment(coords);
            if (type != PathIterator.SEG_CLOSE) {
                geoCoords.add(new LngLatAlt(coords[0] / COORD_PRECISION, coords[1] / COORD_PRECISION));
            }
            if (type == PathIterator.SEG_QUADTO) {
                geoCoords.add(new LngLatAlt(coords[2] / COORD_PRECISION, coords[3] / COORD_PRECISION));
            } else if (type == PathIterator.SEG_CUBICTO) {
                geoCoords.add(new LngLatAlt(coords[2] / COORD_PRECISION, coords[3] / COORD_PRECISION));
                geoCoords.add(new LngLatAlt(coords[4] / COORD_PRECISION, coords[5] / COORD_PRECISION));
            }
            iter.next();
        }
        return geoCoords;
    }

    public static Area createMergedArea(Map.Entry<Object, List<Polygon>> entry) {
        Area area = new Area(entry.getValue().get(0));
        for (int i = 1; i < entry.getValue().size(); i++) {
            area.add(new Area(entry.getValue().get(i)));
        }
        return area;
    }
}
