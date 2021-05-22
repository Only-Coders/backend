package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Admin;

@Repository
public interface AdminRepository extends Neo4jRepository<Admin, String> {
  @Query(
    "MATCH (p:Admin) WHERE toLower(p.email) =~ toLower($email) WITH p MATCH (p)-[h:HAS]-(r:Role) RETURN p, collect(h),collect(r)"
  )
  Optional<Admin> findByEmail(String email);
}
