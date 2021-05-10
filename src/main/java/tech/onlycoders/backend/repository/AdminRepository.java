package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Admin;

@Repository
public interface AdminRepository extends Neo4jRepository<Admin, String> {
  Optional<Admin> findByEmail(String email);
}
