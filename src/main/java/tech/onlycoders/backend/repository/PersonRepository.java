package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Person;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
  @Query(
    "MATCH (p:Person) WHERE toLower(p.email) =~ toLower($email) WITH p MATCH (p)-[h:HAS]-(r:Role) RETURN p, collect(h),collect(r)"
  )
  Optional<Person> findByEmail(String email);
}
