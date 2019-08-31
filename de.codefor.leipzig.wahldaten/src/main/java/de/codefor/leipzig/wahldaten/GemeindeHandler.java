package de.codefor.leipzig.wahldaten;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.codefor.leipzig.wahldaten.Constants.outFolder;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getFeatureCollection;
import static de.codefor.leipzig.wahldaten.utils.MongoUtils.writeToMongoDB;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisConstants.wahlkreisNrToGemeinden;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisFeatureHandler.setWahlkreisToFeature;

public class GemeindeHandler {
    private static final String IN_GEMEINDEN_OSM_GEOJSON = Constants.inFolder + "gemeinden_osm.geojson";
    private static final String FEATURE_PROP_NAME = "name";

    private static final String OUT_GEMEINDEN_SACHSEN_GEOJSON = Constants.docsFolder + "gemeinden_sachsen.geojson";

    public static FeatureCollection getOsmFeatureCollection(ObjectMapper objectMapper) throws IOException {
        return getFeatureCollection(IN_GEMEINDEN_OSM_GEOJSON, objectMapper);
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
        FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
        writeToMongoDB(gemeindenGeojsonFeatures, objectMapper, "sachsen_gemeinden");
    }

    public static FeatureCollection writeGemeindenGeojson(ObjectMapper objectMapper, FeatureCollection featureCollection) throws IOException {
        List<String> allNames = new ArrayList<>();
        List<String> notFoundNames = new ArrayList<>();
        List<String> foundNames = new ArrayList<>();
        FeatureCollection newColl = new FeatureCollection();
        for (Feature feature : featureCollection.getFeatures()) {
            if (!(feature.getGeometry() instanceof Point)) {
                String name = getNameProperty(feature);
                allNames.add(name);
                boolean found = setWahlkreisToFeature(feature, name);
                if (!found) {
                    notFoundNames.add(name);
                } else {
                    foundNames.add(name);
                }
                newColl.add(feature);
            }
        }
        objectMapper.writeValue(new File(OUT_GEMEINDEN_SACHSEN_GEOJSON), newColl);
        writeNamesToFiles(allNames, notFoundNames, foundNames);
        return newColl;
    }

    private static String getNameProperty(Feature feature) {
        Object obj = feature.getProperty(FEATURE_PROP_NAME);
        String name;
        if (obj instanceof String) {
            name = (String) obj;
        } else if (feature.getProperty(FEATURE_PROP_NAME) instanceof char[]) {
            name = String.copyValueOf((char[]) obj);
        } else if (obj == null) {
            name = "unknown";
        } else {
            name = feature.getProperty(FEATURE_PROP_NAME).toString();
        }
        return name;
    }

    private static void writeNamesToFiles(List<String> allNames, List<String> notFoundNames, List<String> foundNames)
            throws IOException {
        writeNameListToFile(outFolder + "names_all.csv", allNames);
        writeNameListToFile(outFolder + "names_not_found.csv", notFoundNames);
        writeNameListToFile(outFolder + "names_found.csv", foundNames);
        List<String> allGiven = wahlkreisNrToGemeinden.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        writeNameListToFile(outFolder + "names_all_given.csv", allGiven);
    }

    private static void writeNameListToFile(String outFileName, List<String> names) throws IOException {
        try (FileWriter fw = new FileWriter(new File(outFileName))) {
            fw.write(String.join("\n", names));
        }
    }
}
