package de.codefor.leipzig.wahldaten.wahlkreis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import static de.codefor.leipzig.wahldaten.Constants.inFolder;
import static de.codefor.leipzig.wahldaten.Constants.docsFolder;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.getOsmFeatureCollection;
import static de.codefor.leipzig.wahldaten.GemeindeHandler.writeGemeindenGeojson;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getFeatureCollection;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getMinXY;
import static de.codefor.leipzig.wahldaten.utils.MongoUtils.writeToMongoDB;

class WahlkreisSvgHandler {
    private static final String IN_WAHLKREISE_WRONGPOS_GEOJSON = inFolder + "wahlkreise_wrongpos.geojson";
    private static final String OUT_WAHLKREISE_2_SACHSEN_GEOJSON = docsFolder + "wahlkreise2_sachsen.geojson";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
        FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
        FeatureCollection wahlkreisGeojsonFeatures = writeWahlkreisGeojsonFromSvg(objectMapper, gemeindenGeojsonFeatures);
        writeToMongoDB(wahlkreisGeojsonFeatures, objectMapper, "sachsen_wahlkreise_svg");
    }

    static FeatureCollection writeWahlkreisGeojsonFromSvg(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures) throws IOException {
        FeatureCollection wahlkreisGeojsonFeatures = getWahlkreisFeatureCollection(objectMapper);
        double[] gemeindenMinXY = getMinXY(gemeindenGeojsonFeatures);
        double[] wahlkreisMinXY = getMinXY(wahlkreisGeojsonFeatures);
        double diffLon = gemeindenMinXY[0] - wahlkreisMinXY[0];
        double factorLon = (gemeindenMinXY[2] - gemeindenMinXY[0]) / (wahlkreisMinXY[2] - wahlkreisMinXY[0]);
        double startLon = gemeindenMinXY[0];
        double startLat = gemeindenMinXY[1];
        double diffLat = gemeindenMinXY[1] - wahlkreisMinXY[1];
        double factorLat = (gemeindenMinXY[3] - gemeindenMinXY[1]) / (wahlkreisMinXY[3] - wahlkreisMinXY[1]);
        return writeTransposedWahlkreisGeojson(objectMapper, wahlkreisGeojsonFeatures, diffLon, diffLat, startLon, startLat, factorLon, factorLat);
    }

    private static FeatureCollection getWahlkreisFeatureCollection(ObjectMapper objectMapper) throws IOException {
        return getFeatureCollection(IN_WAHLKREISE_WRONGPOS_GEOJSON, objectMapper);
    }

    private static FeatureCollection writeTransposedWahlkreisGeojson(ObjectMapper objectMapper, FeatureCollection wahlkreisGeojsonFeatures,
                                                                     double diffLon, double diffLat, double startLon, double startLat,
                                                                     double factorLon, double factorLat) throws IOException {
        FeatureCollection newWahlkreisColl = new FeatureCollection();
        for (Feature feature : wahlkreisGeojsonFeatures.getFeatures()) {
            GeoJsonObject newGeometry = null;
            if (feature.getGeometry() instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) feature.getGeometry();
                MultiPolygon newMultiPolygon = new MultiPolygon();
                for (List<List<LngLatAlt>> list : multiPolygon.getCoordinates()) {
                    for (List<LngLatAlt> coords : list) {
                        List<LngLatAlt> newCoords = getLngLatAlts(diffLon, diffLat, startLon, startLat, factorLon, factorLat, coords);
                        newMultiPolygon.add(new org.geojson.Polygon(newCoords));
                    }
                }
                //newGeometry = multiPolygon;
            } else if (feature.getGeometry() instanceof org.geojson.Polygon) {
                org.geojson.Polygon polygon = (org.geojson.Polygon) feature.getGeometry();
                org.geojson.Polygon newPolygon = new org.geojson.Polygon();
                for (List<LngLatAlt> coords : polygon.getCoordinates()) {
                    List<LngLatAlt> newCoords = getLngLatAlts(diffLon, diffLat, startLon, startLat, factorLon, factorLat, coords);
                    newPolygon.add(newCoords);
                }
                newGeometry = newPolygon;
            }
            if (newGeometry != null) {
                Feature newFeature = new Feature();
                newFeature.setId(feature.getId());
                newFeature.setProperties(feature.getProperties());
                newFeature.setGeometry(newGeometry);
                newWahlkreisColl.add(newFeature);

            }
        }
        objectMapper.writeValue(new File(OUT_WAHLKREISE_2_SACHSEN_GEOJSON), newWahlkreisColl);
        return newWahlkreisColl;
    }

    private static List<LngLatAlt> getLngLatAlts(double diffLon, double diffLat, double startLon, double startLat, double factorLon,
                                                 double factorLat, List<LngLatAlt> coords) {
        return coords.stream().map(lla -> new LngLatAlt(getNewLongitude(diffLon, startLon, factorLon * 0.3, lla.getLongitude()),
                (lla.getLatitude() + diffLat) + ((lla.getLatitude() + diffLat - startLat) * factorLat * 0.3), lla.getAltitude())).collect(Collectors.toList());
    }

    private static double getNewLongitude(double diffLon, double startLon, double factorLon, double longitude) {
        return (longitude + diffLon) + ((longitude + diffLon - startLon) * factorLon);
    }
}
