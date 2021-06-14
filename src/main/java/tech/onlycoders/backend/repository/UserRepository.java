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
    " MATCH (me:User{email: $email})-[:LIVES]->(c:Country) " +
    " OPTIONAL MATCH (tag:Tag)<-[:IS_INTERESTED]-(me) " +
    " OPTIONAL MATCH (p:User)-[:IS_CONNECTED]-(me) " +
    " OPTIONAL MATCH (p2:user)-[:SENDS|:TO]-(:ContactRequest)-[:SENDS|:TO]-(me) " +
    " OPTIONAL MATCH (p3:User)<-[:FOLLOWS]-(me) " +
    " WITH me, c, tag, collect(DISTINCT p)+collect(DISTINCT p2)+collect(DISTINCT p3) as myContacts " +
    " CALL { " +
    "     WITH me, c, tag, myContacts " +
    "     MATCH (c)<-[:LIVES]-(p:User)-[t:IS_CONNECTED*2..2]-(me) " +
    "     WHERE p <> me AND NOT p IN myContacts " +
    "     RETURN p, count(t)*(2^128) AS priority " +
    "     LIMIT $size " +
    "   UNION " +
    "     WITH me, c, myContacts " +
    "     MATCH (p:User)-[t:IS_CONNECTED*2..2]-(me) " +
    "     WHERE p <> me AND  NOT p IN myContacts " +
    "     RETURN p, count(t)*(2^64) AS priority " +
    "     LIMIT $size " +
    "   UNION " +
    "     WITH me, c, myContacts " +
    "     MATCH (p:User)-[:WORKS]-(:WorkPosition)-[:ON]-(t:Workplace)-[:ON]-(:WorkPosition)-[:WORKS]-(me) " +
    "     WHERE p <> me AND NOT p IN myContacts  " +
    "     RETURN p, count(t)*(2^32) AS priority " +
    "     LIMIT $size " +
    "   UNION " +
    "     WITH me, c, tag, myContacts " +
    "     MATCH (c)<-[:LIVES]-(p:User)-[:IS_INTERESTED]->(tag) " +
    "     WHERE tag IS NOT NULL AND p <> me AND NOT p IN myContacts " +
    "     RETURN p, count(tag) AS priority " +
    "     LIMIT $size " +
    "   UNION " +
    "     WITH me, c, myContacts " +
    "     MATCH (c)<-[:LIVES]-(p) " +
    "     WHERE p <> me AND NOT p IN myContacts " +
    "     RETURN p, 0.9 AS priority " +
    "     LIMIT $size " +
    "   UNION " +
    "     WITH me, c, tag, myContacts " +
    "     MATCH (p:User)-[:IS_INTERESTED]->(tag) " +
    "     WHERE tag IS NOT NULL AND p <> me AND NOT p IN myContacts " +
    "     RETURN p, count(tag)/(2^32) AS priority " +
    "     LIMIT $size " +
    " } " +
    " RETURN DISTINCT (p), priority ORDER BY priority DESC " +
    " LIMIT $size; "
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
  Boolean requestHasBeenSent(String sourceCanonicalName, String targetCanonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$sourceCanonicalName})<-[:TO]-(fr:ContactRequest)<-[:SENDS]-(u2:User{canonicalName:$targetCanonicalName}) RETURN count(fr)>0"
  )
  Boolean hasPendingRequest(String sourceCanonicalName, String targetCanonicalName);

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
    " MATCH (User{canonicalName: $canonicalName})-[:IS_CONNECTED]-(u:User) " +
    "   OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    "   OPTIONAL MATCH (u)-[:LIVES]->(c:Country) " +
    "   OPTIONAL MATCH (u)-[]->()<-[:TO]-(r:Reaction{type:'APPROVE'}) " +
    "   WITH u, s, c, r " +
    " WHERE u.fullName =~ $userName AND c.name =~ $countryName AND COALESCE(s.name, '') =~ $skillName " +
    " WITH u, COUNT(r) as medals " +
    " RETURN u{.*, medals: medals} ORDER BY u[$sortField] DESC SKIP $skip LIMIT $size "
  )
  List<User> getMyContacts(
    String canonicalName,
    int skip,
    Integer size,
    String userName,
    String countryName,
    String skillName,
    String sortField
  );

  @Query(
    " MATCH (User{canonicalName: $canonicalName})-[:IS_CONNECTED]-(u:User) " +
    " OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    " OPTIONAL MATCH (u)-[:LIVES]->(c:Country) " +
    "   WITH u, s, c " +
    " WHERE u.fullName =~ $userName AND c.name =~ $countryName AND COALESCE(s.name, '') =~ $skillName  " +
    " RETURN COUNT(DISTINCT(u)) "
  )
  Integer countContacts(String canonicalName, String userName, String countryName, String skillName);

  @Query(
    " MATCH (User{canonicalName: $canonicalName})-[:FOLLOWS]->(u:User) " +
    "   OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    "   OPTIONAL MATCH (u)-[:LIVES]->(c:Country) " +
    "   OPTIONAL MATCH (u)-[]->()<-[:TO]-(r:Reaction{type:'APPROVE'}) " +
    "   WITH u, s, c, r " +
    " WHERE u.fullName =~ $userName AND c.name =~ $countryName AND COALESCE(s.name, '') =~ $skillName " +
    " WITH u, COUNT(r) as medals " +
    " RETURN u{.*, medals: medals} ORDER BY u[$sortField] DESC SKIP $skip LIMIT $size "
  )
  List<User> getMyFollows(
    String canonicalName,
    int skip,
    Integer size,
    String userName,
    String countryName,
    String skillName,
    String sortField
  );

  @Query(
    " MATCH (User{canonicalName: $canonicalName})-[:FOLLOWS]->(u:User) " +
    "   OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    "   OPTIONAL MATCH (u)-[:LIVES]->(c:Country) " +
    "   WITH u, s, c " +
    " WHERE u.fullName =~ $userName AND c.name =~ $countryName AND COALESCE(s.name, '') =~ $skillName " +
    " RETURN COUNT(DISTINCT(u)) "
  )
  Integer countFollows(String canonicalName, String userName, String countryName, String skillName);

  @Query(" MATCH (User{canonicalName:$canonicalName})-[:IS_CONNECTED]-(u:User) RETURN COUNT(DISTINCT(u))")
  Integer countContactsWithOutFilters(String canonicalName);

  @Query(
    "MATCH (u1:User{email: $email}) WITH u1 MATCH (u2:User{canonicalName: $requesterCanonicalName}) CREATE (u1)-[:IS_CONNECTED]->(u2)"
  )
  void addContact(String email, String requesterCanonicalName);

  @Query(
    " MATCH (source:User{canonicalName: $canonicalName })-[:PUBLISH]->(:Post)<-[:TO]-(r:Reaction{type:'APPROVE'})<-[:MAKES]-(u:User) " +
    " WHERE source <> u " +
    " RETURN COUNT(r) "
  )
  Integer countUserMedals(String canonicalName);

  @Query("MATCH (:User{canonicalName:$canonicalName})<-[:FOLLOWS]-(u:User) return count(u)")
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

  @Query("MATCH (u:User{email: $email}) set u += {eliminationDate: null}")
  void removeUserEliminationDate(String email);

  @Query(
    " MATCH (u:User)-[l:LIVES]->(c:Country) " +
    " WHERE (u.fullName =~ $userName OR replace(u.fullName,' ','') =~ $userName) AND c.name =~ $countryName " +
    " OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    " OPTIONAL MATCH (u)-[:PUBLISH]->(:Post)<-[:TO]-(r:Reaction{type:'APPROVE'}) " +
    " WHERE COALESCE(s.name, '') =~ $skillName " +
    " WITH u, l, c, COUNT (r) as medals " +
    " RETURN u{.*, medals: medals}, COLLECT(l), COLLECT(c) " +
    " ORDER BY toLower(u[$sortField]) ASC SKIP $skip LIMIT $size "
  )
  List<User> findAllWithFilters(
    String userName,
    String countryName,
    String skillName,
    String sortField,
    Integer skip,
    Integer size
  );

  @Query(
    " MATCH (u:User)-[l:LIVES]->(c:Country) " +
    " WHERE (u.fullName =~ $userName OR replace(u.fullName,' ','') =~ $userName) AND c.name =~ $countryName " +
    " OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    " OPTIONAL MATCH (u)-[:PUBLISH]->(:Post)<-[:TO]-(r:Reaction{type:'APPROVE'}) " +
    " WHERE COALESCE(s.name, '') =~ $skillName " +
    " WITH u, l, c, COUNT (r) as medals " +
    " RETURN u{.*, medals: medals}, collect(l), collect(c) " +
    " ORDER BY u.medals DESC SKIP $skip LIMIT $size "
  )
  List<User> findAllWithFiltersAndSortByMedals(
    String userName,
    String countryName,
    String skillName,
    int skip,
    Integer size
  );

  @Query(
    " MATCH (u:User) " +
    " OPTIONAL MATCH (u)-[:POSSESS]->(s:Skill) " +
    " OPTIONAL MATCH (u)-[:LIVES]->(c:Country) " +
    "   WITH u, s, c " +
    " WHERE u.fullName =~ $userName AND c.name =~ $countryName AND COALESCE(s.name, '') =~ $skillName  " +
    " RETURN COUNT(DISTINCT(u)) "
  )
  int countWithFilters(String userName, String countryName, String skillName);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName}) " +
    "SET u += {birthDate: $birthDate, firstName: $firstName, lastName: $lastName, imageURI: $imageURI, description: $description, fullName: $fullName}"
  )
  void updateProfile(
    String canonicalName,
    Long birthDate,
    String description,
    String firstName,
    String lastName,
    String imageURI,
    String fullName
  );

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName}) with u " +
    "MATCH (c:Country{code:$countryCode}) with c,u " +
    "MATCH (u)-[l:LIVES]->(:Country) DELETE l with c,u " +
    "MERGE (u)-[:LIVES]->(c)"
  )
  void setCountry(String canonicalName, String countryCode);

  @Query(
    "MATCH (u:User{canonicalName: $canonicalName}) with u " +
    "MATCH (p:GitPlatform{id: $platformId}) with p,u " +
    "MATCH (u)-[g:USES]->(:GitPlatform) DELETE g with p,u " +
    "MERGE (u)-[:USES{username: $userName}]->(p)"
  )
  void setGitProfile(String canonicalName, String userName, String platformId);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[g:USES]->(:GitPlatform) DELETE g")
  void removeGitProfile(String canonicalName);

  @Query("MATCH (:User{canonicalName:$canonicalName})-[g:USES]->(:GitPlatform) SET g += {userName: $userName}")
  void updateGitProfile(String canonicalName, String userName);

  @Query(
    "MATCH (target:User{email: $email})\n" +
    "    OPTIONAL MATCH (target)-[:PUBLISH]->(post:Post)\n" +
    "    OPTIONAL MATCH (target)-[:WORKS]->(workPosition:WorkPosition)\n" +
    "    OPTIONAL MATCH (target)-[:STUDIES]->(degree:Degree)\n" +
    "    OPTIONAL MATCH (target)-[:CREATES]->(report:Report) \n" +
    "    OPTIONAL MATCH (target)-[:SENDS|:TO]-(contactRequest:ContactRequest)\n" +
    "    OPTIONAL MATCH (post)<-[:TO]-(postReaction:Reaction)\n" +
    "    OPTIONAL MATCH (post)<-[:FOR]-(postComment:Comment) \n" +
    "    OPTIONAL MATCH (post)<-[:HAS]-(postReport:Report) \n" +
    "    OPTIONAL MATCH (postReaction)<-[:TO]-(commentReaction:Reaction) \n" +
    "detach delete commentReaction, postReport, postComment, postReaction,\n" +
    "contactRequest, report, degree, workPosition, post, target;"
  )
  void deleteUser(String email);
}
