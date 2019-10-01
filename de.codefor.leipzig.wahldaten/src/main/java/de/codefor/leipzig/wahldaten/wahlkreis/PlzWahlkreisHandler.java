package de.codefor.leipzig.wahldaten.wahlkreis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codefor.leipzig.wahldaten.wahlkreis.model.Wahlkreis;
import de.codefor.leipzig.wahldaten.wahlkreis.model.Wahlkreise;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static de.codefor.leipzig.wahldaten.Constants.inFolder;
import static de.codefor.leipzig.wahldaten.utils.GeojsonUtils.getJsonFileContent;

public class PlzWahlkreisHandler {
    private static String IN_PLZ_WAHLKREIS_JSON = inFolder + "plz_wahlkreise.json";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        List<Wahlkreis> wahlkreise = getWahlkreise(objectMapper).getWahlkreise();
        System.out.println(wahlkreise);
    }

    public static Wahlkreise getWahlkreise(ObjectMapper objectMapper) throws IOException {
        return getJsonFileContent(IN_PLZ_WAHLKREIS_JSON, objectMapper, Wahlkreise.class);
    }
}
