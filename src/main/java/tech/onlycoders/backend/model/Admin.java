package tech.onlycoders.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node({ "Admin", "Person" })
@Data
@NoArgsConstructor
public class Admin extends Person {}
