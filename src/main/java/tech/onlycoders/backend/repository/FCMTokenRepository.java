package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.FCMToken;

@Repository
public interface FCMTokenRepository extends Neo4jRepository<FCMToken, String> {
  Optional<FCMToken> findByDeviceId(String deviceId);

  @Query(" MATCH (t:FCMToken{id: $tokenId})<-[owns:OWNS]-(u:User{canonicalName: $canonicalName}) return count(owns)>0;")
  Boolean verifyIfTokenBelongsToUser(String tokenId, String canonicalName);

  @Query(
    " MATCH (t:FCMToken{id: $tokenId}) WITH t " +
    " MATCH (u:User{canonicalName: $canonicalName}) CREATE (u)-[:OWNS]->(t);"
  )
  void addUserToken(String canonicalName, String tokenId);
}
