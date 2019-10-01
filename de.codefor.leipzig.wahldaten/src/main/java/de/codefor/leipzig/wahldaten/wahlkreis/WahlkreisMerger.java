package de.codefor.leipzig.wahldaten.wahlkreis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;

import java.awt.*;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.codefor.leipzig.wahldaten.Constants.docsFolder;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.getOsmFeatureCollection;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.writeGemeindenGeojson;
import static de.codefor.leipzig.wahldaten.utils.PolygonMerger.*;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisFeatureHandler.createAndAddFeature;

public class WahlkreisMerger {
    private static final String WAHLKREIS_NR = "WahlkreisNr";
    private static final String OUT_WAHLKREISE_SACHSEN_GEOJSON = docsFolder + "wahlkreise_sachsen.geojson";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
        FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
        writeWahlkreisGeojson(objectMapper, gemeindenGeojsonFeatures);
    }

    public static void writeWahlkreisGeojson(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures)
            throws IOException {
        Map<Object, java.util.List<Polygon>> mergeMap = collectPolygonsPerWahlkreis(gemeindenGeojsonFeatures, COORD_PRECISION);
        FeatureCollection newWahlkreisColl = new FeatureCollection();
        for (Map.Entry<Object, java.util.List<Polygon>> entry : mergeMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Area area = createMergedArea(entry);
                List<LngLatAlt> geoCoords = getCoordsFromArea(area);
                if (!geoCoords.isEmpty() && geoCoords.get(0) != geoCoords.get(geoCoords.size()-1)) {
                    geoCoords.add(geoCoords.get(0));
                }
                createAndAddFeature(newWahlkreisColl, entry, geoCoords);
            }
        }
        objectMapper.writeValue(new File(OUT_WAHLKREISE_SACHSEN_GEOJSON), newWahlkreisColl);
    }

    private static Map<Object, List<Polygon>> collectPolygonsPerWahlkreis(FeatureCollection newColl, int precision) {
        Map<Object, List<Polygon>> mergeMap = new HashMap<>();
        for (Feature feature : newColl.getFeatures()) {
            Object wahlKreisNr = feature.getProperty(WAHLKREIS_NR);
            if (wahlKreisNr != null && feature.getGeometry() instanceof org.geojson.Polygon) {
                Polygon polygon = getPolygon(precision, ((org.geojson.Polygon) feature.getGeometry()).getCoordinates());
                List<Polygon> list = getPolygons(mergeMap, feature, wahlKreisNr, polygon);
                mergeMap.put(wahlKreisNr, list);
            } else if (wahlKreisNr != null && feature.getGeometry() instanceof org.geojson.MultiPolygon) {
                org.geojson.MultiPolygon multi = (org.geojson.MultiPolygon) feature.getGeometry();
                for (List<List<LngLatAlt>> coordsList : multi.getCoordinates()) {
                    Polygon polygon = getPolygon(precision, coordsList);
                    List<Polygon> list = getPolygons(mergeMap, feature, wahlKreisNr, polygon);
                    mergeMap.put(wahlKreisNr, list);
                }
            }
        }
        return mergeMap;
    }

    private static Polygon getPolygon(int precision, List<List<LngLatAlt>> coordsList) {
        Polygon polygon = new Polygon();
        for (List<LngLatAlt> list : coordsList) {
            for (LngLatAlt coord : list) {
                polygon.addPoint(Double.valueOf(coord.getLongitude() * precision).intValue(),
                        Double.valueOf(coord.getLatitude() * precision).intValue());
            }
        }
        return polygon;
    }

    private static List<Polygon> getPolygons(Map<Object, List<Polygon>> mergeMap, Feature feature, Object wahlKreisNr,
                                             Polygon polygon) {
        List<Polygon> list;
        if (mergeMap.containsKey(wahlKreisNr)) {
            list = mergeMap.get(feature.getProperty(WAHLKREIS_NR));
        } else {
            list = new ArrayList<>();
        }
        list.add(polygon);
        return list;
    }
}
