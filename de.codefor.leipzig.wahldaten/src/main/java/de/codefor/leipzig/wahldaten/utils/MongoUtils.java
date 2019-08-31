package de.codefor.leipzig.wahldaten.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

public class MongoUtils {

    public static void writeToMongoDB(FeatureCollection geojsonFeatures, ObjectMapper objectMapper, String collectionName) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("joerg");
        MongoCollection<Document> collection = database.getCollection(collectionName);
        for (Feature feature : geojsonFeatures.getFeatures()) {
            try {
                Document doc = Document.parse(objectMapper.writeValueAsString(feature));
                collection.insertOne(doc);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
