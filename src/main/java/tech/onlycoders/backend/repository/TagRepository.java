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
    "   MATCH (t:Tag)<-[:IS_INTERESTED]-(p:Person) WHERE t.name =~ $likeName " +
    "   RETURN DISTINCT(t), count(p) as quantity " +
    "UNION " +
    "   MATCH (t:Tag) " +
    "   RETURN DISTINCT(t), 0 as quantity " +
    "} RETURN DISTINCT(t), quantity ORDER BY quantity DESC SKIP $skip LIMIT $size "
  )
  List<Tag> getTagsByNamePaginated(String likeName, Integer skip, Integer size);

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
    "CALL { " +
    "  MATCH (x:Person)-[:IS_INTERESTED]->(t:Tag)<-[:IS_INTERESTED]-(:Person{canonicalName: $userCanonicalName}) RETURN t, count(x) as quantity UNION " +
    "  MATCH (t:Tag)<-[:IS_INTERESTED]-(:Person{canonicalName: $userCanonicalName}) RETURN t, 0 as quantity " +
    "} RETURN t ORDER BY quantity DESC SKIP $skip LIMIT $size"
  )
  Set<Tag> getFollowedTags(String userCanonicalName, Integer skip, Integer size);

  @Query("MATCH (t:Tag)<-[r:IS_INTERESTED]-(:Person{canonicalName: $userCanonicalName}) RETURN count(r)")
  Integer getAmountOfFollowedTags(String userCanonicalName);
}
