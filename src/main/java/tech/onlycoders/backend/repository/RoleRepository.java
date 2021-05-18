package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Role;

@Repository
public interface RoleRepository extends Neo4jRepository<Role, String> {}
