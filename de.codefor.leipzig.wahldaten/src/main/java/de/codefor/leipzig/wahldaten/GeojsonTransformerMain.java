package de.codefor.leipzig.wahldaten;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeojsonTransformerMain {

	private static final String IN_GEMEINDEN_OSM_GEOJSON = "in/gemeinden_osm.geojson";
	private static final String FEATURE_PROP_NAME = "name";
	private static final String WAHLKREIS_NR = "WahlkreisNr";
	private static final String WAHLKREIS_NAME = "WahlkreisName";

	private static final String OUT_WAHLKREISE_SACHSEN_GEOJSON = "docs/wahlkreise_sachsen.geojson";
	private static final String OUT_GEMEINDEN_SACHSEN_GEOJSON = "docs/gemeinden_sachsen.geojson";

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

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		FeatureCollection featureCollection = getOsmFeatureCollection(objectMapper);
		FeatureCollection gemeindenGeojsonFeatures = writeGemeindenGeojson(objectMapper, featureCollection);
		writeWahlkreisGeojson(objectMapper, gemeindenGeojsonFeatures);
	}

	private static void writeWahlkreisGeojson(ObjectMapper objectMapper, FeatureCollection gemeindenGeojsonFeatures)
			throws IOException, JsonGenerationException, JsonMappingException {
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

	private static FeatureCollection getOsmFeatureCollection(ObjectMapper objectMapper) throws IOException,
			JsonParseException, JsonMappingException, UnsupportedEncodingException, FileNotFoundException {
		File file = new File(IN_GEMEINDEN_OSM_GEOJSON);
		try (Scanner scanner = new Scanner(file, "UTF-8")) {
			scanner.useDelimiter("\\Z");
			try (ByteArrayInputStream inputStream = new ByteArrayInputStream(scanner.next().getBytes("UTF-8"))) {
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

	private static FeatureCollection writeGemeindenGeojson(ObjectMapper objectMapper,
			FeatureCollection featureCollection) throws IOException, JsonGenerationException, JsonMappingException {
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
		writeNameListToFile("out/names_all.csv", allNames);
		writeNameListToFile("out/names_not_found.csv", notFoundNames);
		writeNameListToFile("out/names_found.csv", foundNames);
		List<String> allGiven = wahlkreisNrToGemeinden.values().stream().flatMap(value -> value.stream())
				.collect(Collectors.toList());
		writeNameListToFile("out/names_all_given.csv", allGiven);
	}

	private static void writeNameListToFile(String outFileName, List<String> names) throws IOException {
		try (FileWriter fw = new FileWriter(new File(outFileName))) {
			fw.write(names.stream().collect(Collectors.joining("\n")));
		}
	}
}
