package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.projections.PartialUser;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
  Optional<PartialUser> findByEmail(String email);

  Optional<PartialUser> findByCanonicalName(String canonicalName);

  @Query(
    "CALL { " +
    " MATCH (p:User)-[:IS_INTERESTED]->(t:Tag)<-[:IS_INTERESTED]-(me:User{email: $email})-[:LIVES]->(c:Country) " +
    " WHERE (p)-[:LIVES]->(c) AND (NOT (p)-[]-(:ContactRequest)-[]-(me) AND NOT (p)-[:IS_CONNECTED]-(me)) " +
    " RETURN p, count(t) AS quantity " +
    "UNION " +
    " MATCH (me:User{email:$email})-[:LIVES]->(c:Country)<-[:LIVES]-(p) " +
    " WHERE NOT (p)-[]-(:ContactRequest)-[]-(me) AND NOT (p)-[:IS_CONNECTED]-(me) " +
    " RETURN p, 0 AS quantity " +
    "} RETURN p ORDER BY quantity DESC LIMIT $size;"
  )
  Set<User> findSuggestedUsers(String email, Integer size);

  @Query(
    "MATCH (u:User{canonicalName:$requesterCanonicalName})-[:IS_CONNECTED]-(u2:User{canonicalName:$targetCanonicalName}) RETURN count(u2)>0"
  )
  Boolean areUsersConnected(String requesterCanonicalName, String targetCanonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$sourceCanonicalName})-[:FOLLOWS]->(u2:User{canonicalName:$targetCanonicalName}) RETURN count(u2)>0"
  )
  Boolean isFollowingAnotherUser(String sourceCanonicalName, String targetCanonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$sourceCanonicalName})-[:SENDS]->(fr:ContactRequest)-[:TO]->(u2:User{canonicalName:$targetCanonicalName}) RETURN count(fr)>0"
  )
  Boolean havePendingRequest(String sourceCanonicalName, String targetCanonicalName);

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

  @Query(
    "CALL { " +
    "  MATCH (User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u:User) RETURN u " +
    "UNION " +
    "  MATCH (User{canonicalName:$canonicalName})-[:FOLLOWS]->(u:User) RETURN u " +
    "} RETURN u ORDER BY u.name DESC SKIP $skip LIMIT $size "
  )
  List<User> getMyContacts(String canonicalName, int skip, Integer size);

  @Query(
    "CALL { " +
    "  MATCH (User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u:User) RETURN u " +
    "UNION " +
    "  MATCH (User{canonicalName:$canonicalName})-[:FOLLOWS]->(u:User) RETURN u " +
    "} RETURN count(u)"
  )
  Integer countContacts(String canonicalName);

  @Query(
    "CALL { " +
    "  MATCH (User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u:User) WHERE u.fullName =~ $userName RETURN u" +
    "} RETURN u ORDER BY u.name DESC SKIP $skip LIMIT $size "
  )
  List<User> filterContactsByName(String canonicalName, String userName, int skip, Integer size);

  @Query(
    "CALL { " +
    "  MATCH (User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u:User)-[:LIVES]->(c:Country) WHERE c.name =~ $countryName RETURN u" +
    "} RETURN u ORDER BY u.name DESC SKIP $skip LIMIT $size "
  )
  List<User> filterContactsByCountry(String canonicalName, String countryName, int skip, Integer size);

  @Query(
    "CALL { " +
    "  MATCH (User{canonicalName:$canonicalName})-[r:IS_CONNECTED]-(u:User)-[:LIVES]->(c:Country) WHERE u.fullName =~ $userName AND  c.name =~ $countryName RETURN u" +
    "} RETURN u ORDER BY u.name DESC SKIP $skip LIMIT $size "
  )
  List<User> filterContactsByCountryAndName(
    String canonicalName,
    String userName,
    String countryName,
    int skip,
    Integer size
  );

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

  @Query("MATCH (u:User{id: $id}) SET u += {blocked: $blocked}")
  void setBlockedStatus(String id, Boolean blocked);

  @Query("MATCH (u:User{email: $email}) SET u += {eliminationDate: $eliminationDate}")
  void setEliminationDate(String email, long eliminationDate);

  @Query("MATCH (u:User{id: $userId}) SET u += {defaultPrivacyIsPublic: $isPublic}")
  void updateDefaultPrivacy(String userId, Boolean isPublic);

  @Query("MATCH (:User{id:$user1Id})-[c:IS_CONNECTED]-(:User{id:$user2Id}) DELETE c")
  void removeContact(String user1Id, String user2Id);
}
