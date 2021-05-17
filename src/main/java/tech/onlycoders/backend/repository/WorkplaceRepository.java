package tech.onlycoders.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Workplace;

@Repository
public interface WorkplaceRepository extends Neo4jRepository<Workplace, String> {
  Page<Workplace> findByNameContainingIgnoreCase(String canonicalName, Pageable pageable);
}
