package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
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
}
