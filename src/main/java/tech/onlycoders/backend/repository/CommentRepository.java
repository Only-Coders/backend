package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Comment;
import tech.onlycoders.backend.repository.projections.PartialComment;

@Repository
public interface CommentRepository extends Neo4jRepository<Comment, String> {
  Optional<PartialComment> getById(String commentId);

  @Query("MATCH (:Post{id: $postId})<-[:FOR]-(c:Comment) RETURN count(c)")
  int getPostCommentsQuantity(String postId);

  @Query(
    "MATCH (:Post{id: $postId})<-[:FOR]-(c:Comment)<-[w:WRITES]-(u:User) " +
    "RETURN c, collect(w), collect(u) " +
    "ORDER BY c.createdAt DESC SKIP $skip LIMIT $size"
  )
  List<Comment> getPostComments(String postId, int skip, Integer size);

  @Query("MATCH (c:Comment{id: $commentId}) WITH c MATCH (u:User{id: $userId}) MERGE (u)-[:WRITES]->(c);")
  void linkWithCommenter(String commentId, String userId);
}
