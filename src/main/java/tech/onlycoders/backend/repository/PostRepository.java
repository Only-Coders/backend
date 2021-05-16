package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Post;

@Repository
public interface PostRepository extends Neo4jRepository<Post, String> {
  @Query("MATCH (u:User{email:$email})-[:IS_FAVORITE]->(p:Post) RETURN count(p)")
  int getUserFavoritePostTotalQuantity(String email);

  @Query("MATCH (u:User{email:$email})-[:IS_FAVORITE]->(p:Post) RETURN p  ORDER BY p.id DESC SKIP $skip LIMIT $size")
  List<Post> getUserFavoritePosts(String email, Integer skip, Integer size);
}
