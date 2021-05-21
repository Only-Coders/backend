package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
  Optional<User> findByEmail(String email);

  Optional<User> findByCanonicalName(String canonicalName);

  @Query(
    "CALL {MATCH (p:User)-[:IS_INTERESTED]->(t:Tag)<-[:IS_INTERESTED]-(me:User{email: $email})-[:LIVES]->(c:Country) " +
    "WHERE (p)-[:LIVES]->(c) AND (NOT (p)-[]-(:ContactRequest)-[]-(me) AND NOT (p)-[]-(me)) " +
    "RETURN p, count(t) AS quantity " +
    "UNION " +
    "MATCH (me:User{email:$email})-[:LIVES]->(c:Country)<-[:LIVES]-(p) " +
    "WHERE NOT (p)-[]-(:ContactRequest)-[]-(me) AND NOT (p)-[]-(me) " +
    "RETURN p, 0 AS quantity " +
    "} RETURN p ORDER BY quantity DESC LIMIT $size;"
  )
  Set<User> findSuggestedUsers(String email, Integer size);

  @Query(
    "MATCH (u:User{canonicalName:$requesterCanonicalName})-[IS_CONNECTED]-(u2:User{canonicalName:$targetCanonicalName}) RETURN count(u2)>0"
  )
  Boolean userIsContact(String requesterCanonicalName, String targetCanonicalName);

  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:Skill{canonicalName: $canonicalName}) MERGE (a)-[:POSSESS]->(b)")
  void addSkill(String userId, String canonicalName);

  @Query(
    "MATCH (a:User{id: $userId}) WITH a MATCH (b:Tag{canonicalName: $canonicalName}) MERGE (a)-[:IS_INTERESTED]->(b)"
  )
  void followTag(String userId, String canonicalName);

  @Query("MATCH (a:User{id: $followerId}) WITH a MATCH (b:User{id: $followedId}) MERGE (a)-[:FOLLOWS]->(b)")
  void followUser(String followerId, String followedId);

  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:Post{id: $postId}) MERGE (a)-[:IS_FAVORITE]->(b)")
  void addFavoritePost(String userId, String postId);

  @Query("MATCH (:User{id: $followerId})-[r:FOLLOWS]->(:User{id: $followedId}) delete r")
  void unfollowUser(String followerId, String followedId);
}
