package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.repository.projections.PartialPost;

@Repository
public interface PostRepository extends Neo4jRepository<Post, String> {
  Optional<PartialPost> getById(String postId);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[:IS_FAVORITE]->(p:Post) RETURN count(DISTINCT(p))")
  int getUserFavoritePostTotalQuantity(String canonicalName);

  @Query(
    " MATCH (:User{canonicalName: $canonicalName})-[i:IS_FAVORITE]->(p:Post) " +
    " WITH p, i " +
    " MATCH (u:User)-[r:PUBLISH]->(p) " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " WITH u, r, p, i, rt, rm, t, m" +
    " ORDER BY id(i) DESC " +
    " RETURN p, collect(r), collect(u), collect(rt), collect(t), collect(rm), collect(m) " +
    " SKIP $skip LIMIT $size "
  )
  List<Post> getUserFavoritePosts(String canonicalName, Integer skip, Integer size);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[r:PUBLISH]->(p:Post) " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) " +
    "ORDER BY p.createdAt DESC SKIP $skip LIMIT $size"
  )
  Set<Post> getPosts(String canonicalName, Integer skip, Integer size);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post) RETURN count(p)")
  Integer countUserPosts(String canonicalName);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[r:PUBLISH]->(p:Post{isPublic:true}) " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) " +
    "ORDER BY p.createdAt DESC SKIP $skip LIMIT $size"
  )
  Set<Post> getUserPublicPosts(String canonicalName, Integer skip, Integer size);

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
    "        MATCH (u:User{canonicalName: $canonicalName})-[r:PUBLISH]->(p:Post)\n" +
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

  @Query("MATCH (p:Post{id: $postId}) with p MATCH (c:Comment{id: $commentId}) CREATE (p)<-[r:FOR]-(c)")
  void addComment(String postId, String commentId);

  @Query("MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post{id:$postId}) DETACH DELETE p")
  void removePost(String canonicalName, String postId);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post{id:$postId})<-[:FOR]-(c:Comment) DETACH DELETE c"
  )
  void removeCommentsPost(String canonicalName, String postId);

  @Query(
    "MATCH (u:User{canonicalName:$canonicalName})-[:PUBLISH]->(p:Post{id:$postId})<-[:FOR]-(r:Report) DETACH DELETE r"
  )
  void removeReports(String canonicalName, String postId);

  @Query("MATCH (u:User)-[:PUBLISH]->(:Post{id: $postId}) RETURN u.canonicalName")
  String getPostPublisherCanonicalName(String postId);

  @Query("MATCH (p:Post{id: $postId}) RETURN p.isPublic")
  boolean postIsPublic(String postId);

  @Query(
    "CALL { " +
    "  MATCH (c:Comment{id:$commentId})-[]->(Post)<-[:PUBLISH]-(:User{canonicalName: $canonicalName}) RETURN c " +
    "  UNION " +
    "  MATCH (c:Comment{id: $commentId})<-[:WRITES]-(:User{canonicalName:$canonicalName}) RETURN c " +
    "} DETACH DELETE c RETURN COUNT(c)>=1;"
  )
  Boolean removeComment(String canonicalName, String commentId);

  @Query("MATCH (p:Post{id: $postId}) WITH p MATCH (u:User{id: $userId}) MERGE (u)-[:PUBLISH]->(p);")
  void linkWithPublisher(String postId, String userId);

  @Query("MATCH (p:Post{id: $postId}) WITH p MATCH (u:User{id: $userId}) MERGE (p)-[:MENTIONS]->(u);")
  void mentionUser(String postId, String userId);

  @Query("MATCH (p:Post{id: $postId})-[r:IS_FAVORITE]-(u:User{canonicalName: $canonicalName}) return count(r)>0;")
  Boolean isFavorite(String postId, String canonicalName);

  @Query(
    "CALL{ \n" +
    "        MATCH (:User{canonicalName: $requesterCanonicalName})-[:IS_CONNECTED]-(u:User)" +
    "-[r:PUBLISH]->(p:Post{isPublic:false})-[rt:HAS]->(t:Tag{canonicalName:$tagCanonicalName})\n" +
    "        RETURN p\n" +
    "    UNION\n" +
    "        MATCH (t:Tag{canonicalName:$tagCanonicalName})<-[rt:HAS]-(p:Post{isPublic:true})<-[r:PUBLISH]-(u:User)\n" +
    "        RETURN p\n" +
    "} \n" +
    "RETURN count(p)\n"
  )
  int getPostsByTagQuantity(String requesterCanonicalName, String tagCanonicalName);

  @Query(
    "CALL{ \n" +
    "        MATCH (:User{canonicalName: $requesterCanonicalName})-[:IS_CONNECTED]-(u:User)" +
    "-[r:PUBLISH]->(p:Post{isPublic:false})-[rt:HAS]->(t:Tag{canonicalName:$tagCanonicalName})\n" +
    "        RETURN p,r,u,rt,t\n" +
    "    UNION\n" +
    "        MATCH (t:Tag{canonicalName:$tagCanonicalName})<-[rt:HAS]-(p:Post{isPublic:true})<-[r:PUBLISH]-(u:User)\n" +
    "        RETURN p,r,u,rt,t\n" +
    "} \n" +
    "OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User)\n" +
    "RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) \n" +
    "ORDER BY p.createdAt DESC \n" +
    "SKIP $skip LIMIT $size"
  )
  Set<Post> getPostsByTag(String requesterCanonicalName, String tagCanonicalName, int skip, int size);
}
