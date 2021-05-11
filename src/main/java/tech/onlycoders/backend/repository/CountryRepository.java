package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Country;

@Repository
public interface CountryRepository extends Neo4jRepository<Country, String> {
  List<Country> findByNameContainingIgnoreCase(String name);
  Page<Country> findByNameContainingIgnoreCase(String organizationName, PageRequest pageRequest);
}
