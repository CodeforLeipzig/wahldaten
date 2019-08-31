package de.codefor.leipzig.wahldaten.wahlkreis;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisConstants.wahlkreisNrToGemeinden;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisConstants.wahlkreisNrToName;

public class WahlkreisFeatureHandler {
    private static final String WAHLKREIS_NR = "WahlkreisNr";
    private static final String WAHLKREIS_NAME = "WahlkreisName";

    static void createAndAddFeature(FeatureCollection newWahlkreisColl, Map.Entry<Object, java.util.List<Polygon>> entry,
                                            List<LngLatAlt> geoCoords) {
        Feature feature = new Feature();
        feature.setProperty(WAHLKREIS_NR, entry.getKey());
        feature.setProperty(WAHLKREIS_NAME, wahlkreisNrToName.get(entry.getKey()));
        String wahlkreisOrte = "<ul>" + wahlkreisNrToGemeinden.get(entry.getKey()).stream()
                .map(wk -> "<li>" + wk + "</li>").collect(Collectors.joining()) + "</ul>";
        feature.setProperty("WahlkreisOrte", wahlkreisOrte);
        feature.setGeometry(new org.geojson.Polygon(geoCoords));
        newWahlkreisColl.add(feature);
    }

    public static boolean setWahlkreisToFeature(Feature feature, String name) {
        boolean found = false;
        for (Map.Entry<String, List<String>> entry : wahlkreisNrToGemeinden.entrySet()) {
            if (entry.getValue().contains(name)) {
                found = true;
                feature.setProperty(WAHLKREIS_NR, entry.getKey());
                feature.setProperty(WAHLKREIS_NAME, wahlkreisNrToName.get(entry.getKey()));
                break;
            }
        }
        return found;
    }
}
