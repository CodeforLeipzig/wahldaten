package de.codefor.leipzig.wahldaten.wahlkreis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Wahlkreise {
    @JsonProperty("Wahlkreise")
    private List<Wahlkreis> wahlkreise;
}
