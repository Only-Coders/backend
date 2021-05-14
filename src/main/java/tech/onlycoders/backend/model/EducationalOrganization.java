package tech.onlycoders.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node({ "EducationalOrganization", "Organization" })
@Data
@NoArgsConstructor
public class EducationalOrganization extends Organization {}
