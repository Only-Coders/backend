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

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u2:User) RETURN u2")
  List<User> getContacts(String canonicalName);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u2:User) RETURN count(u2)")
  Integer countContacts(String canonicalName);

  @Query(
    "MATCH (u1:User{email: $email}) WITH u1 MATCH (u2:User{canonicalName: $requesterCanonicalName}) CREATE (u1)-[:IS_CONNECTED]->(u2)"
  )
  void addContact(String email, String requesterCanonicalName);

  @Query("MATCH (u:User) WHERE u.fullName =~ $likeName RETURN (u) SKIP $skip LIMIT $size")
  List<User> findByPartialName(String likeName, Integer skip, Integer size);

  @Query("MATCH (u:User) WHERE u.fullName =~ $likeName RETURN count(u)")
  Integer countByPartialName(String likeName);

  @Query(
    "MATCH (:User{canonicalName:$canonicalName})-[:PUBLISH]->(:Post)<-[:TO]-(r:Reaction{type:'APPROVE'}) return count(r)"
  )
  Integer countUserMedals(String canonicalName);

  @Query("MATCH (:User{canonicalName:$canonicalName})<-[FOLLOWS]-(u:User) return count(u)")
  Integer countUserFollowers(String canonicalName);

  @Query("MATCH (:User{email: $email})-[f:IS_FAVORITE]->(:Post{id: $postId}) DELETE f")
  void removeFavoritePost(String email, String postId);

  @Query("MATCH (:User{id: $userId})-[r:IS_INTERESTED]->(:Tag{canonicalName: $canonicalName}) DELETE r")
  void unFollowTag(String userId, String canonicalName);
}
