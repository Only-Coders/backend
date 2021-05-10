package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
  Optional<User> findByEmail(String email);

  Optional<User> findByCanonicalName(String canonicalName);
}
