package de.codefor.leipzig.wahldaten.postleitzahlen;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import dev.morphia.geo.Geometry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Entity("postleitzahlen")
@Getter
@Setter
@NoArgsConstructor
public class PLZ {
    @Id
    private ObjectId id;
    @Property("properties")
    private Map<String, String> props = new HashMap<>();
    @Property("geometry")
    private Geometry geometry;
}
