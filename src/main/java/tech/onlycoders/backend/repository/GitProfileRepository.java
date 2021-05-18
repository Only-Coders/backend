package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.GitProfile;

@Repository
public interface GitProfileRepository extends Neo4jRepository<GitProfile, Long> {}
