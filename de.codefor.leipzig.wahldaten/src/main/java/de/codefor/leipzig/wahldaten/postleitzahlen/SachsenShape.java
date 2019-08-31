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

@Entity("sachsen_shape")
@Getter
@Setter
@NoArgsConstructor
public class SachsenShape {
    @Id
    private ObjectId id;
    @Property("geometry")
    private Geometry geometry;
}
