package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.FCMToken;

@Repository
public interface FCMTokenRepository extends Neo4jRepository<FCMToken, String> {
  @Query("MATCH (t:FCMToken{id: $id}) DETACH DELETE t;")
  void deleteById(String id);

  @Query(
    " MATCH (t:FCMToken{id: $tokenId}) WITH t " +
    " MATCH (u:User{canonicalName: $canonicalName}) CREATE (u)-[:OWNS]->(t);"
  )
  void addUserToken(String canonicalName, String tokenId);
}
