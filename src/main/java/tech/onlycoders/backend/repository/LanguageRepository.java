package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Language;

@Repository
public interface LanguageRepository extends Neo4jRepository<Language, String> {}
