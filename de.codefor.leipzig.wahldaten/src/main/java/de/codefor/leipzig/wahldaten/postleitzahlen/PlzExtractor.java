package de.codefor.leipzig.wahldaten.postleitzahlen;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.geo.GeoJson;
import dev.morphia.geo.MultiPolygon;
import dev.morphia.geo.Polygon;
import dev.morphia.query.Query;
import org.geojson.FeatureCollection;

import java.io.IOException;

import static de.codefor.leipzig.wahldaten.Constants.inFolder;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getFeatureCollection;
import static de.codefor.leipzig.wahldaten.utils.MongoUtils.writeToMongoDB;

public class PlzExtractor {

    public static void main(String [] args) throws IOException {
        //writePLZDeutschland();
        //writeSachsenGeojson();

        Morphia morphia = new Morphia();
        morphia.mapPackage("de.codefor.leipzig.wahldaten.postleitzahlen");
        Datastore datastore = morphia.createDatastore(new MongoClient(), "joerg");

        Polygon sachsenPolygon = (Polygon) datastore.find(SachsenShape.class).first().getGeometry();
        MultiPolygon multiPolygon = GeoJson.multiPolygon(sachsenPolygon);

        Query<PLZ> query = datastore.find(PLZ.class);
        query.criteria("geometry").within(multiPolygon);
        String targetCollection = "postleitzahlen_sachsen";
        datastore.createAggregation(PLZ.class).match(query).out(targetCollection, PLZ.class);

    }

    private static void writeSachsenGeojson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection sachsenFeatureCollection = getFeatureCollection(inFolder + "sachsen.geojson", objectMapper);
        writeToMongoDB(sachsenFeatureCollection, objectMapper, "sachsen_shape");
    }

    private static void writePLZDeutschland() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection plzFeatureCollection = getFeatureCollection("/home/joerg/Downloads/plz-gebiete.geojson", objectMapper);
        writeToMongoDB(plzFeatureCollection, objectMapper, "postleitzahlen");
    }
}
