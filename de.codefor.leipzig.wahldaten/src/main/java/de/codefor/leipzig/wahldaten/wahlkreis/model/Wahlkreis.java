package de.codefor.leipzig.wahldaten.wahlkreis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Wahlkreis {
    @JsonProperty("Nummer")
    private String nummer;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Gemeinden")
    private List<String> gemeinden = new ArrayList<>();
    @JsonProperty("Ortsteile")
    private List<String> ortsteile = new ArrayList<>();
    @JsonProperty("Postleitzahlen")
    private List<String> postleitzahlen = new ArrayList<>();
}
