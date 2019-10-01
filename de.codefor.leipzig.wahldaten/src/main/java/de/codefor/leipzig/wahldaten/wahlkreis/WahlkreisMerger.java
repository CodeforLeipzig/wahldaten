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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.codefor.leipzig.wahldaten.Constants.docsFolder;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.getOsmFeatureCollection;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.writeGemeindenGeojson;
import static de.codefor.leipzig.wahldaten.utils.PolygonMerger.*;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisFeatureHandler.createAndAddFeature;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisFeatureHandler.createAndAddMultiFeature;

public class WahlkreisMerger {
    private static final String WAHLKREIS_NR = "WahlkreisNr";
    private static final String OUT_WAHLKREISE_SACHSEN_GEOJSON = docsFolder + "wahlkreise_sachsen.geojson";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
        FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
        //writeWahlkreisGeojson(objectMapper, gemeindenGeojsonFeatures);
        writeWahlkreisGeojsonMulti(objectMapper, gemeindenGeojsonFeatures);
    }

    public static void writeWahlkreisGeojson(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures)
            throws IOException {
        Map<Object, java.util.List<Polygon>> mergeMap = collectPolygonsPerWahlkreis(gemeindenGeojsonFeatures, COORD_PRECISION);
        FeatureCollection newWahlkreisColl = new FeatureCollection();
        for (Map.Entry<Object, java.util.List<Polygon>> entry : mergeMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Area area = createMergedArea(entry);
                List<LngLatAlt> geoCoords = getCoordsFromArea(area);
                if (!geoCoords.isEmpty() && geoCoords.get(0).getLatitude() != geoCoords.get(geoCoords.size() - 1).getLatitude()
                        && geoCoords.get(0).getLongitude() != geoCoords.get(geoCoords.size() - 1).getLongitude()) {
                    geoCoords.add(geoCoords.get(0));
                } else {
                    createAndAddFeature(newWahlkreisColl, entry, geoCoords);
                }
            }
        }
        objectMapper.writeValue(new File(OUT_WAHLKREISE_SACHSEN_GEOJSON), newWahlkreisColl);
    }

    public static void writeWahlkreisGeojsonMulti(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures)
            throws IOException {
        Map<Object, java.util.List<org.geojson.Polygon>> mergeMap = collectGeojsonPolygonsPerWahlkreis(gemeindenGeojsonFeatures, COORD_PRECISION);
        FeatureCollection newWahlkreisColl = new FeatureCollection();
        for (Map.Entry<Object, java.util.List<org.geojson.Polygon>> entry : mergeMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                List<List<LngLatAlt>> latLons = entry.getValue().stream().map(val -> val.getCoordinates().stream()).flatMap(val -> val).collect(Collectors.toList());
                createAndAddMultiFeature(newWahlkreisColl, entry, latLons);
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

    private static Map<Object, List<org.geojson.Polygon>> collectGeojsonPolygonsPerWahlkreis(FeatureCollection newColl, int precision) {
        Map<Object, List<org.geojson.Polygon>> mergeMap = new HashMap<>();
        for (Feature feature : newColl.getFeatures()) {
            Object wahlKreisNr = feature.getProperty(WAHLKREIS_NR);
            if (wahlKreisNr != null && feature.getGeometry() instanceof org.geojson.Polygon) {
                org.geojson.Polygon polygon = (org.geojson.Polygon) feature.getGeometry();
                List<org.geojson.Polygon> list = getGeojsonPolygons(mergeMap, feature, wahlKreisNr, polygon);
                mergeMap.put(wahlKreisNr, list);
            } else if (wahlKreisNr != null && feature.getGeometry() instanceof org.geojson.MultiPolygon) {
                org.geojson.MultiPolygon multi = (org.geojson.MultiPolygon) feature.getGeometry();
                for (List<List<LngLatAlt>> coordsList : multi.getCoordinates()) {
                    for (List<LngLatAlt> coords : coordsList) {
                        org.geojson.Polygon polygon = new org.geojson.Polygon(coords);
                        List<org.geojson.Polygon> list = getGeojsonPolygons(mergeMap, feature, wahlKreisNr, polygon);
                        mergeMap.put(wahlKreisNr, list);
                    }
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

    private static List<org.geojson.Polygon> getGeojsonPolygons(Map<Object, List<org.geojson.Polygon>> mergeMap, Feature feature, Object wahlKreisNr,
                                                         org.geojson.Polygon polygon) {
        List<org.geojson.Polygon> list;
        if (mergeMap.containsKey(wahlKreisNr)) {
            list = mergeMap.get(feature.getProperty(WAHLKREIS_NR));
        } else {
            list = new ArrayList<>();
        }
        list.add(polygon);
        return list;
    }
}
