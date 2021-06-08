package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Tag;

@Repository
public interface TagRepository extends Neo4jRepository<Tag, String> {
  Optional<Tag> findByCanonicalName(String canonicalName);

  @Query("MATCH (t:Tag{canonicalName:$canonicalName})<-[:IS_INTERESTED]-(p:Person) RETURN count(p)")
  Long getFollowerQuantity(String canonicalName);

  @Query("MATCH (t:Tag) WHERE t.canonicalName =~ $likeName RETURN count(t)")
  int getTagQuantityByName(String likeName);

  @Query(
    "CALL { " +
    "   MATCH (t:Tag)<-[:IS_INTERESTED]-(p:Person) WHERE t.canonicalName =~ $likeName " +
    "   RETURN DISTINCT(t), count(p) as quantity " +
    "UNION " +
    "   MATCH (t:Tag) WHERE t.canonicalName =~ $likeName " +
    "   RETURN DISTINCT(t), 0 as quantity " +
    "} RETURN DISTINCT(t), quantity ORDER BY quantity DESC SKIP $skip LIMIT $size "
  )
  Set<Tag> getTagsByNamePaginated(String likeName, Integer skip, Integer size);

  @Query("MATCH (t:Tag) RETURN count(t)")
  int getTagQuantity();

  @Query(
    "CALL { " +
    "   MATCH (t:Tag)<-[:IS_INTERESTED]-(p:Person) " +
    "   RETURN DISTINCT(t), count(p) as quantity " +
    "UNION " +
    "   MATCH (t:Tag) RETURN t, 0 as quantity" +
    "} RETURN DISTINCT(t), quantity ORDER BY quantity DESC SKIP $skip LIMIT $size"
  )
  List<Tag> getTagsPaginated(Integer skip, Integer size);

  @Query(
    " MATCH (t:Tag)<-[:IS_INTERESTED]-(:User{canonicalName: $userCanonicalName}) " +
    " OPTIONAL MATCH (u:User)-[:IS_INTERESTED]->(t)  " +
    " WITH t, u  WHERE t.canonicalName =~ $tagCanonicalName " +
    " RETURN t, count(COALESCE(u.canonicalName, 0)) as quantity " +
    " ORDER BY quantity DESC, t.canonicalName ASC SKIP $skip LIMIT $size"
  )
  Set<Tag> getFollowedTags(String userCanonicalName, String tagCanonicalName, Integer skip, Integer size);

  @Query(
    " MATCH (t:Tag)<-[r:IS_INTERESTED]-(:Person{canonicalName: $userCanonicalName}) " +
    " WHERE t.canonicalName =~ $tagCanonicalName " +
    " RETURN count(r)"
  )
  Integer getAmountOfFollowedTags(String userCanonicalName, String tagCanonicalName);

  @Query(
    " MATCH (me:User{canonicalName: $canonicalName}) " +
    " OPTIONAL MATCH (tags:Tag)<-[:IS_INTERESTED]-(me) " +
    " OPTIONAL MATCH (myContacts:User)-[:IS_CONNECTED]-(me) " +
    " OPTIONAL MATCH (myFollowed:User)<-[:FOLLOWS]-(me) " +
    " WITH me, collect(tags) as myTags, myFollowed, myContacts " +
    " CALL { " +
    "     WITH myTags, myContacts " +
    "     MATCH  (:User)-[i:IS_INTERESTED]-(t:Tag)<-[:IS_INTERESTED]-(myContacts) " +
    "     WHERE NOT t IN myTags " +
    "     RETURN t, count(i)*(2^64) AS priority " +
    "     LIMIT $limit " +
    "   UNION " +
    "     WITH myTags, myFollowed " +
    "     MATCH (:User)-[i:IS_INTERESTED]-(t:Tag)<-[:IS_INTERESTED]-(myFollowed) " +
    "     WHERE NOT t IN myTags " +
    "     RETURN t, count(i)*(2^32) AS priority " +
    "     LIMIT $limit " +
    "   UNION " +
    "     WITH  myTags " +
    "     MATCH (t:Tag)<-[i:IS_INTERESTED]-(:User) " +
    "     WHERE NOT t IN myTags " +
    "     RETURN t , count(i) AS priority " +
    "     LIMIT $limit " +
    "   UNION " +
    "     WITH  myTags " +
    "     MATCH (t:Tag) " +
    "     WHERE NOT t IN myTags " +
    "     RETURN t , 0 AS priority " +
    "     LIMIT $limit " +
    " } " +
    " RETURN DISTINCT(t), priority ORDER BY priority DESC " +
    " LIMIT $limit "
  )
  Set<Tag> findSuggestedTags(String canonicalName, Integer limit);
}
