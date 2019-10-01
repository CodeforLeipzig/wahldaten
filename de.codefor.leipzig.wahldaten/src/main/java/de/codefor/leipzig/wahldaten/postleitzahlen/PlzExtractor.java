package de.codefor.leipzig.wahldaten.postleitzahlen;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import de.codefor.leipzig.wahldaten.wahlkreis.model.Wahlkreis;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.geo.GeoJson;
import dev.morphia.geo.MultiPolygon;
import dev.morphia.geo.Polygon;
import dev.morphia.query.Query;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.codefor.leipzig.wahldaten.Constants.inFolder;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getFeatureCollection;
import static de.codefor.leipzig.wahldaten.utils.MongoUtils.writeToMongoDB;
import static de.codefor.leipzig.wahldaten.wahlkreis.PlzWahlkreisHandler.getWahlkreise;
import static de.codefor.leipzig.wahldaten.wahlkreis.WahlkreisMerger.writeWahlkreisGeojson;

public class PlzExtractor {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        //writePLZDeutschland(objectMapper);
        //writeSachsenGeojson();
        //writePlzsWithinSachsenGeojson(objectMapper);
        FeatureCollection sachsenPlzGeojson = writeSachsenPlzGeojson(objectMapper);
        writeWahlkreisGeojson(objectMapper, sachsenPlzGeojson);
    }

    private static FeatureCollection writeSachsenPlzGeojson(ObjectMapper objectMapper) throws IOException {
        List<Wahlkreis> wahlkreise = getWahlkreise(objectMapper).getWahlkreise();
        Set<String> plzs = wahlkreise.stream().map(wahlkreis -> wahlkreis.getPostleitzahlen().stream())
                .flatMap(plz -> plz).collect(Collectors.toSet());
        Map<String, List<String>> wkToPlzs = wahlkreise.stream().collect(Collectors.toMap(wk -> wk.getNummer(), wk -> wk.getPostleitzahlen()));

        FeatureCollection allPlzGeos = getFeatureCollection("D:/plz.geojson", objectMapper);
        FeatureCollection newColl = new FeatureCollection();
        for (Feature feature : allPlzGeos.getFeatures()) {
            String plz = String.valueOf(feature.getProperties().get("plz"));
            if (plz != null && plzs.contains(plz)) {
                String wahlkreisNr = findWahlkreisNr(wkToPlzs, plz);
                feature.getProperties().put("WahlkreisNr", wahlkreisNr);
                newColl.add(feature);
            }
        }
        objectMapper.writeValue(new File("D:/sachsen_plz.geojson"), newColl);
        return newColl;
    }

    private static String findWahlkreisNr(Map<String, List<String>> wkToPlzs, String plz) {
        Optional<Map.Entry<String, List<String>>> found = wkToPlzs.entrySet().stream().filter(entry -> entry.getValue().contains(plz)).findFirst();
        if (found.isPresent()) {
            return found.get().getKey();
        }
        return null;
    }

    private static void writePlzsWithinSachsenGeojson() {
        Morphia morphia = new Morphia();
        morphia.mapPackage("de.codefor.leipzig.wahldaten.postleitzahlen");
        Datastore datastore = morphia.createDatastore(new MongoClient(), "joerg");

        Polygon sachsenPolygon = (Polygon) datastore.find(SachsenShape.class).first().getGeometry();
        MultiPolygon multiPolygon = GeoJson.multiPolygon(sachsenPolygon);

        Query<PLZ> query = datastore.find(PLZ.class);
        query.criteria("geometry").within(sachsenPolygon);
        String targetCollection = "postleitzahlen_sachsen";
        datastore.createAggregation(PLZ.class).match(query).out(targetCollection, PLZ.class);
    }

    private static void writeSachsenGeojson(ObjectMapper objectMapper) throws IOException {
        FeatureCollection sachsenFeatureCollection = getFeatureCollection(inFolder + "sachsen.geojson", objectMapper);
        writeToMongoDB(sachsenFeatureCollection, objectMapper, "sachsen_shape");
    }

    private static void writePLZDeutschland(ObjectMapper objectMapper) throws IOException {
        FeatureCollection plzFeatureCollection = getFeatureCollection("/home/joerg/Schreibtisch/plz.geojson", objectMapper);
        writeToMongoDB(plzFeatureCollection, objectMapper, "postleitzahlen");
    }
}
