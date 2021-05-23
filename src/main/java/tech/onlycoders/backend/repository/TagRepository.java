package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Tag;

@Repository
public interface TagRepository extends Neo4jRepository<Tag, String> {
  Optional<Tag> findByCanonicalName(String canonicalName);

  Page<Tag> findByCanonicalNameContainingIgnoreCase(String tagName, PageRequest pageRequest);

  @Query("MATCH (t:Tag{canonicalName:$canonicalName})<-[:IS_INTERESTED]-(p:Person) RETURN count(p)")
  Long getFollowerQuantity(String canonicalName);

  @Query("MATCH (t:Tag) WHERE t.canonicalName =~ $likeName RETURN count(t)")
  int getTagQuantityByName(String likeName);

  @Query(
    "CALL {MATCH (t:Tag)<-[:IS_INTERESTED]-(p:Person) WHERE t.canonicalName =~ $likeName " +
    "RETURN t, count(p) as quantity UNION " +
    "MATCH (t:Tag) WHERE t.canonicalName =~ $likeName " +
    "RETURN t, 0  as quantity} RETURN t ORDER BY quantity DESC SKIP $skip LIMIT $size "
  )
  List<Tag> getTagsByNamePaginated(String likeName, Integer skip, Integer size);

  @Query("MATCH (t:Tag) RETURN count(t)")
  int getTagQuantity();

  @Query(
    "CALL { " +
    "MATCH (t:Tag)<-[:IS_INTERESTED]-(p:Person) RETURN t, count(p) as quantity " +
    "UNION " +
    "MATCH (t:Tag) RETURN t, 0 as quantity} " +
    "RETURN t ORDER BY quantity DESC SKIP $skip LIMIT $size"
  )
  List<Tag> getTagsPaginated(Integer skip, Integer size);
}
