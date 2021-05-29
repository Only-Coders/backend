package tech.onlycoders.backend.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Reaction;
import tech.onlycoders.backend.model.ReactionType;

@Repository
public interface ReactionRepository extends Neo4jRepository<Reaction, String> {
  @Query("MATCH (:Post{id: $postId})<-[:TO]-(r:Reaction{type: $type}) RETURN count(r)")
  long getPostReactionQuantity(String postId, ReactionType type);

  @Query(
    "MATCH (p:Post{id: $postId})<-[t:TO]-(r:Reaction)<-[m:MAKES]-(u:User{canonicalName: $canonicalName}) RETURN r, collect(m),collect(u)"
  )
  Optional<Reaction> getPostUserReaction(String canonicalName, String postId);

  @Query("MATCH (:Comment{id: $id})<-[:TO]-(r:Reaction{type: $type}) RETURN count(r)")
  Long getCommentReactionQuantity(String id, ReactionType type);

  @Query("MATCH (:Comment{id: $id})<-[:TO]-(r:Reaction)<-[:MAKES]-(:User{canonicalName: $canonicalName}) RETURN r")
  // TODO / BUG: This should be Optional
  Reaction getCommentUserReaction(String canonicalName, String id);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[:MAKES]->(r:Reaction)-[:TO]->(p:Post{id:$postId}) DETACH DELETE r"
  )
  void removeReaction(String canonicalName, String postId);

  @Query("MATCH (p:Post{id: $postId}) WITH p MATCH (r:Reaction{id: $reactionId}) MERGE (r)-[:TO]->(p)")
  void linkWithPost(String reactionId, String postId);
}
