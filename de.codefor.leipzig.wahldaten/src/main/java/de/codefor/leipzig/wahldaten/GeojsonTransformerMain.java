package de.codefor.leipzig.wahldaten;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.geojson.Point;
import org.geojson.*;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class GeojsonTransformerMain {

    private static final String repoFolder = "/media/Daten/git/wahldaten/";
    private static final String projectFolder = repoFolder + "de.codefor.leipzig.wahldaten/";
    private static final String inFolder = projectFolder + "in/";
    private static final String outFolder = projectFolder + "out/";
    private static final String docsFolder = repoFolder + "docs/";
    private static final String IN_GEMEINDEN_OSM_GEOJSON = inFolder + "gemeinden_osm.geojson";
    private static final String FEATURE_PROP_NAME = "name";
    private static final String WAHLKREIS_NR = "WahlkreisNr";
    private static final String WAHLKREIS_NAME = "WahlkreisName";

    private static final String OUT_WAHLKREISE_SACHSEN_GEOJSON = docsFolder + "wahlkreise_sachsen.geojson";
    private static final String OUT_GEMEINDEN_SACHSEN_GEOJSON = docsFolder + "gemeinden_sachsen.geojson";

    public static final String IN_WAHLKREISE_WRONGPOS_GEOJSON = inFolder + "wahlkreise_wrongpos.geojson";
    public static final String OUT_WAHLKREISE_2_SACHSEN_GEOJSON = docsFolder + "wahlkreise2_sachsen.geojson";

    private static int COORD_PRECISION = 10000000;

    // https://de.wikipedia.org/wiki/Liste_der_Landtagswahlkreise_in_Sachsen
    private static Map<String, String> wahlkreisNrToName = new HashMap<>();
    private static Map<String, List<String>> wahlkreisNrToGemeinden = new HashMap<>();

    static {
        fillWahlkreisNrToName();
        fillWahlkreisNrToGemeinden();
    }

    private static void fillWahlkreisNrToName() {
        wahlkreisNrToName.put("1", "Vogtland 1");
        wahlkreisNrToName.put("2", "Vogtland 2");
        wahlkreisNrToName.put("3", "Vogtland 3");
        wahlkreisNrToName.put("4", "Vogtland 4");
        wahlkreisNrToName.put("5", "Zwickau 1");
        wahlkreisNrToName.put("6", "Zwickau 2");
        wahlkreisNrToName.put("7", "Zwickau 3");
        wahlkreisNrToName.put("8", "Zwickau 4");
        wahlkreisNrToName.put("9", "Zwickau 5");
        wahlkreisNrToName.put("10", "Chemnitz 1");
        wahlkreisNrToName.put("11", "Chemnitz 2");
        wahlkreisNrToName.put("12", "Chemnitz 3");
        wahlkreisNrToName.put("13", "Erzgebirge 1");
        wahlkreisNrToName.put("14", "Erzgebirge 2");
        wahlkreisNrToName.put("15", "Erzgebirge 3");
        wahlkreisNrToName.put("16", "Erzgebirge 4");
        wahlkreisNrToName.put("17", "Erzgebirge 5");
        wahlkreisNrToName.put("18", "Mittelsachsen 1");
        wahlkreisNrToName.put("19", "Mittelsachsen 2");
        wahlkreisNrToName.put("20", "Mittelsachsen 3");
        wahlkreisNrToName.put("21", "Mittelsachsen 4");
        wahlkreisNrToName.put("22", "Mittelsachsen 5");
        wahlkreisNrToName.put("23", "Leipzig Land 1");
        wahlkreisNrToName.put("24", "Leipzig Land 2");
        wahlkreisNrToName.put("25", "Leipzig Land 3");
        wahlkreisNrToName.put("26", "Leipzig Land 4");
        wahlkreisNrToName.put("27", "Leipzig 1");
        wahlkreisNrToName.put("28", "Leipzig 2");
        wahlkreisNrToName.put("29", "Leipzig 3");
        wahlkreisNrToName.put("30", "Leipzig 4");
        wahlkreisNrToName.put("31", "Leipzig 5");
        wahlkreisNrToName.put("32", "Leipzig 6");
        wahlkreisNrToName.put("33", "Leipzig 7");
        wahlkreisNrToName.put("34", "Nordsachsen 1");
        wahlkreisNrToName.put("35", "Nordsachsen 2");
        wahlkreisNrToName.put("36", "Nordsachsen 3");
        wahlkreisNrToName.put("37", "Meißen 1");
        wahlkreisNrToName.put("38", "Meißen 2");
        wahlkreisNrToName.put("39", "Meißen 3");
        wahlkreisNrToName.put("40", "Meißen 4");
        wahlkreisNrToName.put("41", "Dresden 1");
        wahlkreisNrToName.put("42", "Dresden 2");
        wahlkreisNrToName.put("43", "Dresden 3");
        wahlkreisNrToName.put("44", "Dresden 4");
        wahlkreisNrToName.put("45", "Dresden 5");
        wahlkreisNrToName.put("46", "Dresden 6");
        wahlkreisNrToName.put("47", "Dresden 7");
        wahlkreisNrToName.put("48", "Sächsische Schweiz-Osterzgebirge 1");
        wahlkreisNrToName.put("49", "Sächsische Schweiz-Osterzgebirge 2");
        wahlkreisNrToName.put("50", "Sächsische Schweiz-Osterzgebirge 3");
        wahlkreisNrToName.put("51", "Sächsische Schweiz-Osterzgebirge 4");
        wahlkreisNrToName.put("52", "Bautzen 1");
        wahlkreisNrToName.put("53", "Bautzen 2");
        wahlkreisNrToName.put("54", "Bautzen 3");
        wahlkreisNrToName.put("55", "Bautzen 4");
        wahlkreisNrToName.put("56", "Bautzen 5");
        wahlkreisNrToName.put("57", "Görlitz 1");
        wahlkreisNrToName.put("58", "Görlitz 2");
        wahlkreisNrToName.put("59", "Görlitz 3");
        wahlkreisNrToName.put("60", "Görlitz 4");
    }

    private static void fillWahlkreisNrToGemeinden() {
        wahlkreisNrToGemeinden.put("1", Arrays.asList("Plauen"));
        wahlkreisNrToGemeinden.put("2",
                Arrays.asList("Adorf", "Bad Elster", "Markneukirchen", "Oelsnitz", "Pausa-Mühltroff",
                        "Schöneck,Bad Brambach", "Bergen", "Bösenbrunn", "Eichigt", "Mühlental", "Reuth", "Rosenbach",
                        "Theuma", "Tirpersdorf", "Triebel", "Weischlitz", "Werda"));
        wahlkreisNrToGemeinden.put("3", Arrays.asList("Auerbach", "Falkenstein", "Klingenthal", "Treuen", "Ellefeld",
                "Grünbach", "Muldenhammer", "Neuensalz", "Neustadt"));
        wahlkreisNrToGemeinden.put("4", Arrays.asList("Elsterberg", "Lengenfeld", "Mylau", "Netzschkau", "Reichenbach",
                "Rodewisch", "Heinsdorfergrund", "Limbach", "Neumark", "Pöhl", "Steinberg"));
        wahlkreisNrToGemeinden.put("5", Arrays.asList("Hartenstein", "Kirchberg", "Wildenfels", "Wilkau-Haßlau",
                "Crinitzberg", "Hartmannsdorf", "Hirschfeld", "Langenweißbach", "Lichtentanne", "Mülsen", "Reinsdorf"));
        wahlkreisNrToGemeinden.put("6", Arrays.asList("Crimmitschau", "Werdau", "Dennheritz", "Fraureuth",
                "Langenbernsdorf", "Neukirchen", "West der Stadt Zwickau im Landkreis Zwickau"));
        wahlkreisNrToGemeinden.put("7",
                Arrays.asList("die Stadtbezirke Mitte, Ost, Nord und Süd der Stadt Zwickau im Landkreis Zwickau"));
        wahlkreisNrToGemeinden.put("8", Arrays.asList("Glauchau", "Lichtenstein", "Meerane", "Waldenburg", "Bernsdorf",
                "Oberwiera", "Remse", "St. Egidien"));
        wahlkreisNrToGemeinden.put("9", Arrays.asList("Hohenstein-Ernstthal", "Limbach-Oberfrohna", "Oberlungwitz",
                "Callenberg", "Gersdorf", "Niederfrohna"));
        wahlkreisNrToGemeinden.put("10",
                Arrays.asList("von der Stadt Chemnitz die Stadtteile: Altendorf", "Grüna", "Hutholz", "Kaßberg",
                        "Mittelbach", "Morgenleite", "Rabenstein", "Reichenbrand", "Röhrsdorf", "Rottluff",
                        "Schloßchemnitz", "Siegmar", "Stelzendorf"));
        wahlkreisNrToGemeinden.put("11",
                Arrays.asList("von der Stadt Chemnitz die Stadtteile: Borna-Heinersdorf", "Ebersdorf", "Furth",
                        "Gablenz", "Glösa-Draisdorf", "Hilbersdorf", "Lutherviertel", "Sonnenberg", "Wittgensdorf",
                        "Yorckgebiet", "Zentrum"));
        wahlkreisNrToGemeinden.put("12",
                Arrays.asList("von der Stadt Chemnitz die Stadtteile: Adelsberg", "Altchemnitz", "Bernsdorf",
                        "Einsiedel", "Erfenschlag", "Euba", "Harthau", "Helbersdorf", "Kapellenberg", "Kappel",
                        "Klaffenbach", "Kleinolbersdorf-Altenhain", "Markersdorf", "Reichenhain", "Schönau"));
        wahlkreisNrToGemeinden.put("13",
                Arrays.asList("Lugau", "Oelsnitz", "Stollberg", "Thalheim", "Amtsberg", "Auerbach", "Burkhardtsdorf",
                        "Gornsdorf", "Hohndorf", "Jahnsdorf", "Neukirchen", "Niederdorf", "Niederwürschnitz"));
        wahlkreisNrToGemeinden.put("14", Arrays.asList("Aue", "Eibenstock", "Schneeberg", "Bad Schlema", "Bockau",
                "Schönheide", "Stützengrün", "Zschorlau"));
        wahlkreisNrToGemeinden.put("15", Arrays.asList("Elterlein", "Grünhain-Beierfeld", "Johanngeorgenstadt",
                "Lauter-Bernsbach", "Lößnitz", "Schwarzenberg", "Zwönitz", "Breitenbrunn", "Raschau-Markersbach"));
        wahlkreisNrToGemeinden.put("16",
                Arrays.asList("Annaberg-Buchholz", "Ehrenfriedersdorf", "Geyer", "Jöhstadt", "Oberwiesenthal",
                        "Scheibenberg", "Schlettau", "Thum", "Bärenstein", "Crottendorf", "Gelenau", "Königswalde",
                        "Mildenau", "Sehmatal", "Wiesenbad"));
        wahlkreisNrToGemeinden.put("17",
                Arrays.asList("Marienberg", "Olbernhau", "Pockau-Lengefeld", "Wolkenstein", "Zschopau", "Börnichen",
                        "Borstendorf", "Deutschneudorf", "Drebach", "Gornau", "Großolbersdorf", "Großrückerswalde",
                        "Grünhainichen", "Heidersdorf", "Pfaffroda", "Seiffen"));
        wahlkreisNrToGemeinden.put("18",
                Arrays.asList("Augustusburg", "Brand-Erbisdorf", "Flöha", "Oederan", "Sayda", "Dorfchemnitz",
                        "Eppendorf", "Großhartmannsdorf", "Leubsdorf", "Mulda", "Neuhausen", "Niederwiesa",
                        "Rechenberg-Bienenmühle"));
        wahlkreisNrToGemeinden.put("19", Arrays.asList("Frauenstein", "Freiberg", "Großschirma",
                "Bobritzsch-Hilbersdorf", "Halsbrücke", "Lichtenberg", "Oberschöna", "Reinsberg", "Weißenborn"));
        wahlkreisNrToGemeinden.put("20", Arrays.asList("Frankenberg", "Hainichen", "Mittweida", "Altmittweida", "Erlau",
                "Kriebstein", "Lichtenau", "Rossau", "Striegistal"));
        wahlkreisNrToGemeinden.put("21", Arrays.asList("Döbeln", "Hartha", "Leisnig", "Roßwein", "Waldheim",
                "Großweitzschen", "Mochau", "Ostrau", "Zschaitz-Ottewig"));
        wahlkreisNrToGemeinden.put("22",
                Arrays.asList("Burgstädt", "Geringswalde", "Lunzenau", "Penig", "Rochlitz", "Claußnitz",
                        "Hartmannsdorf", "Königsfeld", "Königshain-Wiederau", "Mühlau", "Seelitz", "Taura",
                        "Wechselburg", "Zettlitz"));
        wahlkreisNrToGemeinden.put("23", Arrays.asList("Landkreis Leipzig: z. B. Borna und Geithain"));
        wahlkreisNrToGemeinden.put("24", Arrays.asList("Landkreis Leipzig: z. B. Markkleeberg und Markranstädt"));
        wahlkreisNrToGemeinden.put("25", Arrays.asList("Landkreis Leipzig: z. B. Grimma"));
        wahlkreisNrToGemeinden.put("26", Arrays.asList("Landkreis Leipzig: z. B. Wurzen"));
        wahlkreisNrToGemeinden.put("27", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("28", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("29", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("30", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("31", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("32", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("33", Arrays.asList("Leipzig"));
        wahlkreisNrToGemeinden.put("34",
                Arrays.asList("Delitzsch", "Schkeuditz", "Krostitz", "Löbnitz", "Rackwitz", "Schönwölkau", "Wiedemar"));
        wahlkreisNrToGemeinden.put("35", Arrays.asList("Landkreis Nordsachsen: z. B. Eilenburg"));
        wahlkreisNrToGemeinden.put("36", Arrays.asList("Landkreis Nordsachsen: z. B. Oschatz und Torgau"));
        wahlkreisNrToGemeinden.put("37", Arrays
                .asList("Landkreis Meißen: z. B. Riesa (entstand aus dem ehemaligen Wahlkreis Riesa-Großenhain 1)"));
        wahlkreisNrToGemeinden.put("38", Arrays.asList(
                "Landkreis Meißen: z. B. Großenhain und Radeburg (entstand aus dem ehemaligen Wahlkreis Riesa-Großenhain 2)"));
        wahlkreisNrToGemeinden.put("39",
                Arrays.asList("Landkreis Meißen: z. B. Meißen (entstand aus dem ehemaligen Wahlkreis Meißen 1)"));
        wahlkreisNrToGemeinden.put("40", Arrays.asList("Coswig", "Moritzburg", "Radebeul"));
        wahlkreisNrToGemeinden.put("41",
                Arrays.asList("Dresden (u. a. Äußere Neustadt", "Klotzsche", "Weixdorf", "Schönfeld-Weißig)"));
        wahlkreisNrToGemeinden.put("42", Arrays.asList("Dresden (u. a. Prohlis", "Leuben", "Loschwitz)"));
        wahlkreisNrToGemeinden.put("43", Arrays.asList("Dresden (u. a. Plauen", "Leubnitz-Neuostra", "Lockwitz)"));
        wahlkreisNrToGemeinden.put("44", Arrays.asList("Dresden (u. a. Blasewitz)"));
        wahlkreisNrToGemeinden.put("45",
                Arrays.asList("Dresden (u. a. Innere Altstadt", "Johannstadt", "Innere Neustadt)"));
        wahlkreisNrToGemeinden.put("46", Arrays.asList("Dresden (u. a. Löbtau", "Gorbitz", "Cossebaude)"));
        wahlkreisNrToGemeinden.put("47", Arrays.asList("Dresden (Stadtbezirk Pieschen",
                "vom Stadtbezirk Altstadt die statistischen Stadtteile Friedrichstadt und Wilsdruffer Vorstadt/Seevorstadt-West",
                "vom Stadtbezirk Cotta der statistische Stadtteil Cotta mit Friedrichstadt-Südwest der kreisfreien Stadt Dresden)"));
        wahlkreisNrToGemeinden.put("48", Arrays.asList("Landkreis Sächsische Schweiz-Osterzgebirge: z. B. Freital"));
        wahlkreisNrToGemeinden.put("49",
                Arrays.asList("Landkreis Sächsische Schweiz-Osterzgebirge: z. B. Dippoldiswalde"));
        wahlkreisNrToGemeinden.put("50", Arrays.asList("Landkreis Sächsische Schweiz-Osterzgebirge: z. B. Pirna"));
        wahlkreisNrToGemeinden.put("51", Arrays.asList("Landkreis Sächsische Schweiz-Osterzgebirge: z. B. Sebnitz"));
        wahlkreisNrToGemeinden.put("52",
                Arrays.asList("Bischofswerda", "Schirgiswalde-Kirschau", "Wilthen", "Burkau", "Cunewalde",
                        "Demitz-Thumitz", "Frankenthal", "Göda", "Großharthau", "Großpostwitz", "Neukirch", "Obergurig",
                        "Rammenau", "Schmölln-Putzkau", "Sohland", "Steinigtwolmsdorf"));
        wahlkreisNrToGemeinden.put("53",
                Arrays.asList("Elstra", "Großröhrsdorf", "Kamenz", "Pulsnitz", "Arnsdorf", "Bretnig-Hauswalde",
                        "Crostwitz", "Großnaundorf", "Haselbachtal", "Lichtenberg", "Nebelschütz", "Ohorn",
                        "Panschwitz-Kuckau", "Räckelwitz", "Ralbitz-Rosenthal", "Steina"));
        wahlkreisNrToGemeinden.put("54", Arrays.asList("Bernsdorf", "Königsbrück", "Lauta", "Radeberg", "Wittichenau",
                "Laußnitz", "Neukirch", "Oßling", "Ottendorf-Okrilla", "Schwepnitz", "Wachau"));
        wahlkreisNrToGemeinden.put("55", Arrays.asList("Hoyerswerda", "Elsterheide", "Königswartha", "Lohsa",
                "Neschwitz", "Puschwitz", "Radibor", "Spreetal"));
        wahlkreisNrToGemeinden.put("56", Arrays.asList("Bautzen", "Weißenberg", "Doberschau-Gaußig", "Großdubrau",
                "Hochkirch", "Kubschütz", "Malschwitz"));
        wahlkreisNrToGemeinden.put("57",
                Arrays.asList("Bad Muskau", "Niesky", "Rothenburg", "Weißwasser", "Boxberg", "Gablenz", "Groß Düben",
                        "Hähnichen", "Hohendubrau", "Horka", "Kodersdorf", "Krauschwitz", "Kreba-Neudorf", "Mücka",
                        "Neißeaue", "Quitzdorf am See", "Rietschen", "Schleife", "Schöpstal", "Trebendorf", "Waldhufen",
                        "Weißkeißel"));
        wahlkreisNrToGemeinden.put("58",
                Arrays.asList("Görlitz", "Reichenbach", "Königshain", "Markersdorf", "Vierkirchen"));
        wahlkreisNrToGemeinden.put("59",
                Arrays.asList("Bernstadt a. d. Eigen", "Ebersbach-Neugersdorf", "Herrnhut", "Löbau",
                        "Neusalza-Spremberg", "Ostritz", "Beiersdorf", "Dürrhennersdorf", "Großschweidnitz", "Kottmar",
                        "Lawalde", "Oppach", "Rosenbach", "Schönau-Berzdorf a. d. Eigen", "Schönbach"));
        wahlkreisNrToGemeinden.put("60", Arrays.asList("Seifhennersdorf", "Zittau", "Bertsdorf-Hörnitz", "Großschönau",
                "Hainewalde", "Jonsdorf", "Leutersdorf", "Mittelherwigsdorf", "Oderwitz", "Olbersdorf", "Oybin"));
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
        FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
        //writeToMongoDB(gemeindenGeojsonFeatures, objectMapper, "sachsen_gemeinden");
        FeatureCollection wahlkreisGeojsonFeatures = writeWahlkreisGeojsonFromSvg(objectMapper, gemeindenGeojsonFeatures);
        //writeToMongoDB(wahlkreisGeojsonFeatures, objectMapper, "sachsen_wahlkreise_svg");
        writeWahlkreisGeojson(objectMapper, gemeindenGeojsonFeatures);
    }

    private static void writeToMongoDB(FeatureCollection gemeindenGeojsonFeatures, ObjectMapper objectMapper, String collectionName) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("joerg");
        MongoCollection<Document> collection = database.getCollection(collectionName);
        for (Feature feature : gemeindenGeojsonFeatures.getFeatures()) {
            try {
                Document doc = Document.parse(objectMapper.writeValueAsString(feature));
                collection.insertOne(doc);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private static FeatureCollection writeWahlkreisGeojsonFromSvg(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures) throws IOException {
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

    private static double[] getMinXY(FeatureCollection gemeindenGeojsonFeatures) {
        return gemeindenGeojsonFeatures.getFeatures().stream().map(f -> getBbox(f.getGeometry())).reduce(GeojsonTransformerMain::minBbox).orElse(new double[] {0, 0});
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

    private static void writeWahlkreisGeojson(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures)
            throws IOException {
        Map<Object, List<Polygon>> mergeMap = collectPolygonsPerWahlkreis(gemeindenGeojsonFeatures, COORD_PRECISION);
        FeatureCollection newWahlkreisColl = new FeatureCollection();
        for (Entry<Object, List<Polygon>> entry : mergeMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Area area = createMergedArea(entry);
                List<LngLatAlt> geoCoords = getCoordsFromArea(area);
                createAndAddFeature(newWahlkreisColl, entry, geoCoords);
            }
        }
        objectMapper.writeValue(new File(OUT_WAHLKREISE_SACHSEN_GEOJSON), newWahlkreisColl);
    }

    private static void createAndAddFeature(FeatureCollection newWahlkreisColl, Entry<Object, List<Polygon>> entry,
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

    private static List<LngLatAlt> getCoordsFromArea(Area area) {
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

    private static Area createMergedArea(Entry<Object, List<Polygon>> entry) {
        Area area = new Area(entry.getValue().get(0));
        for (int i = 1; i < entry.getValue().size(); i++) {
            area.add(new Area(entry.getValue().get(i)));
        }
        return area;
    }

    private static FeatureCollection getWahlkreisFeatureCollection(ObjectMapper objectMapper) throws IOException {
        return getFeatureCollection(IN_WAHLKREISE_WRONGPOS_GEOJSON, objectMapper);
    }

    private static FeatureCollection getOsmFeatureCollection(ObjectMapper objectMapper) throws IOException {
        return getFeatureCollection(IN_GEMEINDEN_OSM_GEOJSON, objectMapper);
    }

    private static FeatureCollection getFeatureCollection(String path, ObjectMapper objectMapper) throws IOException {
        File file = new File(path);
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\Z");
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(scanner.next().getBytes(StandardCharsets.UTF_8.name()))) {
                return objectMapper.readValue(inputStream, FeatureCollection.class);
            }
        }
    }

    private static Map<Object, List<Polygon>> collectPolygonsPerWahlkreis(FeatureCollection newColl, int precision) {
        Map<Object, List<Polygon>> mergeMap = new HashMap<>();
        for (Feature feature : newColl.getFeatures()) {
            Object wahlKreisNr = feature.getProperty(WAHLKREIS_NR);
            if (wahlKreisNr != null && feature.getGeometry() instanceof org.geojson.Polygon) {
                Polygon polygon = getPolygon(precision, feature);
                List<Polygon> list = getPolygons(mergeMap, feature, wahlKreisNr, polygon);
                mergeMap.put(wahlKreisNr, list);
            }
        }
        return mergeMap;
    }

    private static Polygon getPolygon(int precision, Feature feature) {
        Polygon polygon = new Polygon();
        org.geojson.Polygon geojsonPolygon = (org.geojson.Polygon) feature.getGeometry();
        for (List<LngLatAlt> list : geojsonPolygon.getCoordinates()) {
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

    private static FeatureCollection writeGemeindenGeojson(ObjectMapper objectMapper, FeatureCollection featureCollection) throws IOException {
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

    private static boolean setWahlkreisToFeature(Feature feature, String name) {
        boolean found = false;
        for (Entry<String, List<String>> entry : wahlkreisNrToGemeinden.entrySet()) {
            if (entry.getValue().contains(name)) {
                found = true;
                feature.setProperty(WAHLKREIS_NR, entry.getKey());
                feature.setProperty(WAHLKREIS_NAME, wahlkreisNrToName.get(entry.getKey()));
                break;
            }
        }
        return found;
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
