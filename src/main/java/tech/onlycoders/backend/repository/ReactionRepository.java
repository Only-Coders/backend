package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Reaction;
import tech.onlycoders.backend.model.ReactionType;

@Repository
public interface ReactionRepository extends Neo4jRepository<Reaction, String> {
  @Query("MATCH (:Post{id: $id})<-[:TO]-(r:Reaction{type: $type}) RETURN count(r)")
  long getPostReactionQuantity(String id, ReactionType type);

  @Query("MATCH (:Post{id: $id})<-[:TO]-(r:Reaction)<-[:MAKES]-(:User{canonicalName: $canonicalName}) RETURN r")
  Reaction getPostUserReaction(String canonicalName, String id);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post{id:$postId})<-[:TO]-(r:Reaction) DETACH DELETE r"
  )
  void removeReaction(String canonicalName, Integer postId);
}
