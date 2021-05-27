package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Comment;

@Repository
public interface CommentRepository extends Neo4jRepository<Comment, String> {
  @Query("MATCH (:Post{id: $postId})<-[:FOR]-(c:Comment) RETURN count(c)")
  int getPostCommentsQuantity(String postId);

  @Query(
    "MATCH (:Post{id: $postId})<-[:FOR]-(c:Comment)<-[w:WRITES]-(u:User) " +
    "RETURN c, collect(w), collect(u) " +
    "ORDER BY c.createdAt DESC SKIP $skip LIMIT $size"
  )
  List<Comment> getPostComments(String postId, int skip, Integer size);
}
