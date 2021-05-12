package tech.onlycoders.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.EducationalOrganization;
import tech.onlycoders.backend.model.Organization;

@Repository
public interface EducationalOrganizationRepository extends Neo4jRepository<EducationalOrganization, String> {
  Page<EducationalOrganization> findByNameContainingIgnoreCase(String canonicalName, Pageable pageable);
}
