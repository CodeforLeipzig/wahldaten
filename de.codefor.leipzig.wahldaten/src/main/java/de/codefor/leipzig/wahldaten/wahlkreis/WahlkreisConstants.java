package de.codefor.leipzig.wahldaten.wahlkreis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codefor.leipzig.wahldaten.wahlkreis.model.Wahlkreise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WahlkreisConstants {
    // https://de.wikipedia.org/wiki/Liste_der_Landtagswahlkreise_in_Sachsen
    static Map<String, String> wahlkreisNrToName = new HashMap<>();
    public static Map<String, List<String>> wahlkreisNrToGemeinden = new HashMap<>();
    public static Map<String, List<String>> wahlkreisNrToOrtsteile = new HashMap<>();
    public static Map<String, List<String>> wahlkreisNrToPLZs = new HashMap<>();

    static {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        try {
            Wahlkreise wahlkreise = PlzWahlkreisHandler.getWahlkreise(objectMapper);
            wahlkreisNrToName = wahlkreise.getWahlkreise().stream().collect(Collectors.toMap(wk -> wk.getNummer(), wk -> wk.getName()));
            wahlkreisNrToGemeinden = wahlkreise.getWahlkreise().stream().collect(Collectors.toMap(wk -> wk.getNummer(), wk -> wk.getGemeinden()));
            wahlkreisNrToOrtsteile = wahlkreise.getWahlkreise().stream().collect(Collectors.toMap(wk -> wk.getNummer(), wk -> wk.getOrtsteile()));
            wahlkreisNrToPLZs = wahlkreise.getWahlkreise().stream().collect(Collectors.toMap(wk -> wk.getNummer(), wk -> wk.getPostleitzahlen()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
