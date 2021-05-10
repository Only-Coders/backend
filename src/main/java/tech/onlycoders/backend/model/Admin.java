package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node({ "Admin", "Person" })
@Data
@NoArgsConstructor
public class Admin extends Person {}
