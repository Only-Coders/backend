package tech.onlycoders.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Institute;

@Repository
public interface InstituteRepository extends Neo4jRepository<Institute, String> {
  Page<Institute> findByNameContainingIgnoreCase(String canonicalName, Pageable pageable);
}
