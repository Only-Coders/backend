package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node({ "EducationalOrganization", "Organization" })
@Data
@NoArgsConstructor
public class EducationalOrganization extends Organization {}
