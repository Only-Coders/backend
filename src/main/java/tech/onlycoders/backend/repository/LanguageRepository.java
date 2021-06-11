package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Language;

@Repository
public interface LanguageRepository extends Neo4jRepository<Language, String> {
  @Query("MATCH (:User{canonicalName:$canonicalName})-[:SPEAKS]->(l:Language) RETURN l")
  Language getUserLanguage(String canonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName}) with u " +
    "MATCH (l:Language{code:$code}) with u,l " +
    "MATCH (u)-[s:SPEAKS]->(:Language) DELETE s with u,l " +
    "MERGE (u)-[:SPEAKS]->(l)"
  )
  void setUserLanguage(String canonicalName, String code);
}
