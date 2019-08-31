package de.codefor.leipzig.wahldaten.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GeojsonUtils {
    public static FeatureCollection getFeatureCollection(String path, ObjectMapper objectMapper) throws IOException {
        return getJsonFileContent(path, objectMapper, FeatureCollection.class);
    }

    public static <T> T getJsonFileContent(String path, ObjectMapper objectMapper, Class<T> clazz) throws IOException {
        File file = new File(path);
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\Z");
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(scanner.next().getBytes(StandardCharsets.UTF_8.name()))) {
                return objectMapper.readValue(inputStream, clazz);
            }
        }
    }

    public static double[] getMinXY(FeatureCollection featureCollection) {
        return featureCollection.getFeatures().stream().map(f -> getBbox(f.getGeometry())).reduce(GeojsonUtils::minBbox).orElse(new double[] {0, 0});
    }

    private static double [] getBbox(GeoJsonObject geometry) {
        double [] minMax = new double[] {1000, 1000, -1000, -1000};
        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (LngLatAlt coord : multiPolygon.getCoordinates().stream().flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList())) {
                bbox(minMax, coord);
            }
        } else if (geometry instanceof org.geojson.Polygon) {
            org.geojson.Polygon polygon = (org.geojson.Polygon) geometry;
            for (LngLatAlt coord : polygon.getCoordinates().stream().flatMap(Collection::stream).collect(Collectors.toList())) {
                bbox(minMax, coord);
            }
        }
        return minMax;
    }

    private static void bbox(double[] minMax, LngLatAlt coord) {
        minMax[0] = Math.min(coord.getLongitude(), minMax[0]);
        minMax[1] = Math.min(coord.getLatitude(), minMax[1]);
        minMax[2] = Math.max(coord.getLongitude(), minMax[2]);
        minMax[3] = Math.max(coord.getLatitude(), minMax[3]);
    }

    private static double[] minBbox(double[] bbox1, double[] bbox2) {
        if (bbox1.length != 4 || bbox2.length != 4) {
            throw new IllegalArgumentException("Bbox should have size 4");
        }
        double[] minBbox = new double[4];
        minBbox[0] = Math.min(bbox1[0], bbox2[0]);
        minBbox[1] = Math.min(bbox1[1], bbox2[1]);
        minBbox[2] = Math.max(bbox1[2], bbox2[2]);
        minBbox[3] = Math.max(bbox1[3], bbox2[3]);
        return minBbox;
    }
}
