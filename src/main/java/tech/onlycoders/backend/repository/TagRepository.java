package tech.onlycoders.backend.repository;

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
}
