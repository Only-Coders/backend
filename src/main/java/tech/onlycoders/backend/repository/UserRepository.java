package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
  @Query("MATCH (u:User{ email: $email }) return u limit 1")
  Optional<User> findOneByEmail(String email);

  @Query("MATCH (u:User{ canonicalName: $canonicalName }) return u limit 1")
  Optional<User> findOneByCanonicalName(String canonicalName);

  Optional<User> findByEmail(String email);

  Optional<User> findByCanonicalName(String canonicalName);

  @Query(
    "CALL {MATCH (p:User)-[:IS_INTERESTED]->(t:Tag)<-[:IS_INTERESTED]-(me:User{email:$email})-[:LIVES]->(c:Country)\n" +
    "            WHERE (p)-[:LIVES]->(c)\n" +
    "            RETURN p , count(t) AS quantity\n" +
    "    UNION\n" +
    "\n" +
    "    MATCH (me:User{email:$email})-[:LIVES]->(c:Country)<-[:LIVES]-(p)\n" +
    "            RETURN p, 0 AS quantity\n" +
    "    \n" +
    "} RETURN p ORDER BY quantity DESC LIMIT $size;"
  )
  List<User> findSuggestedUsers(String email, Integer size);

  @Query(
    "MATCH (u:User{canonicalName:$requesterCanonicalName})-[IS_CONNECTED]-(u2:User{canonicalName:$targetCanonicalName}) RETURN count(u2)>0"
  )
  Boolean userIsContact(String requesterCanonicalName, String targetCanonicalName);
}
