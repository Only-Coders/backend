package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Person;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
  Optional<Person> findByEmail(String email);
  Optional<Person> findByCanonicalName(String canonicalName);
}
