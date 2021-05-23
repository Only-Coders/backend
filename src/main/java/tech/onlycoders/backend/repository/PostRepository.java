package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Set;
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

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[r:PUBLISH]->(p:Post) RETURN p, collect(r), collect(u) ORDER BY p.createdAt DESC SKIP $skip LIMIT $size"
  )
  List<Post> getPosts(String canonicalName, Integer skip, Integer size);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post) RETURN count(p)")
  Integer countUserPosts(String canonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[r:PUBLISH]->(p:Post{isPublic:true}) RETURN p, collect(r), collect(u)  ORDER BY p.createdAt DESC SKIP $skip LIMIT $size"
  )
  List<Post> getUserPublicPosts(String canonicalName, Integer skip, Integer size);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post{isPublic:true}) RETURN count(p)")
  Integer countUserPublicPosts(String canonicalName);

  @Query(
    "CALL{ \n" +
    "        MATCH (:User{canonicalName: $canonicalName})-[:FOLLOWS]->(u:User)-[r:PUBLISH]->(p:Post{isPublic:true})\n" +
    "        RETURN p,r,u\n" +
    "    UNION\n" +
    "        MATCH (:User{canonicalName: $canonicalName})-[:IS_CONNECTED]-(u:User)-[r:PUBLISH]->(p:Post)\n" +
    "        RETURN p,r,u\n" +
    "    UNION\n" +
    "        MATCH (:User{canonicalName: $canonicalName})-[:IS_INTERESTED]->(:Tag)<-[:HAS]-(p:Post{isPublic:true})<-[r:PUBLISH]-(u:User)\n" +
    "        RETURN p,r,u\n" +
    "} \n" +
    "OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag)\n" +
    "OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User)\n" +
    "RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) \n" +
    "ORDER BY p.createdAt DESC \n" +
    "SKIP $skip LIMIT $size"
  )
  Set<Post> getFeedPosts(String canonicalName, int skip, Integer size);

  @Query(
    "CALL{ \n" +
    "    MATCH (:User{canonicalName: $canonicalName})-[:FOLLOWS]->(u:User)-[r:PUBLISH]->(p:Post{isPublic:true}) RETURN p\n" +
    "    UNION\n" +
    "    MATCH (:User{canonicalName: $canonicalName})-[:IS_CONNECTED]-(u:User)-[r:PUBLISH]->(p:Post) RETURN p\n" +
    "    UNION\n" +
    "    MATCH (:User{canonicalName: $canonicalName})-[:IS_INTERESTED]->(:Tag)<-[:HAS]-(p:Post{isPublic:true}) RETURN p\n" +
    "} RETURN count(p)"
  )
  int getFeedPostsQuantity(String canonicalName);

  @Query("MATCH (:Post{id: $id})<-[:FOR]-(c:Comment) RETURN count(c)")
  long getPostCommentsQuantity(String id);
}
