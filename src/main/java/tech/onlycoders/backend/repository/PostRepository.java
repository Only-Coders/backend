package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.PostType;
import tech.onlycoders.backend.repository.projections.PartialPost;
import tech.onlycoders.backend.repository.projections.PartialUser;

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
    " MATCH (target:User{canonicalName: $canonicalName}) " +
    " WITH target " +
    " CALL{ " +
    "    WITH target " +
    "    MATCH (target)-[r:PUBLISH]->(p:Post{isPublic: true }) " +
    "    RETURN p, r " +
    "  UNION " +
    "    WITH target " +
    "    MATCH (:User{canonicalName: $requesterCanonicalName})<-[:MENTIONS]-(p:Post)<-[r:PUBLISH]-(target) " +
    "    RETURN p, r " +
    " } " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN DISTINCT(p), collect(r), collect(target), collect(rm), collect(m), collect(rt), collect(t) " +
    " ORDER BY p.createdAt DESC " +
    " SKIP $skip " +
    " LIMIT $size "
  )
  Set<Post> getUserPublicPosts(String requesterCanonicalName, String canonicalName, Integer skip, Integer size);

  @Query(
    " MATCH (target:User{canonicalName: $canonicalName}) " +
    " WITH target " +
    " CALL{ " +
    "    WITH target " +
    "    MATCH (target)-[r:PUBLISH]->(p:Post{isPublic: true }) " +
    "    RETURN p, r " +
    "  UNION " +
    "    WITH target " +
    "    MATCH (:User{canonicalName: $requesterCanonicalName})<-[:MENTIONS]-(p:Post)<-[r:PUBLISH]-(target) " +
    "    RETURN p, r " +
    " } " +
    " RETURN COUNT(DISTINCT(p))"
  )
  Integer countUserPublicPosts(String requesterCanonicalName, String canonicalName);

  @Query(
    " MATCH (target:User{canonicalName: $canonicalName}) " +
    " WITH target " +
    " CALL{ " +
    "   WITH target " +
    "   MATCH (target)-[:FOLLOWS]->(u:User)-[r:PUBLISH]->(p:Post{isPublic: true }) " +
    "   RETURN p, r, u " +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[:IS_CONNECTED]-(u:User)-[r:PUBLISH]->(p:Post) " +
    "   RETURN p, r, u " +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[r:PUBLISH]->(p:Post) " +
    "   RETURN p, r, target as u " +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[:IS_INTERESTED]->(:Tag)<-[:HAS]-(p:Post{isPublic: true })<-[r:PUBLISH]-(u:User) " +
    "   RETURN p, r, u " +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)<-[:MENTIONS]-(p:Post)<-[r:PUBLISH]-(u:User) " +
    "   RETURN p, r, u " +
    " } " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN DISTINCT(p), collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) " +
    " ORDER BY p.createdAt DESC " +
    " SKIP $skip " +
    " LIMIT $size "
  )
  Set<Post> getFeedPosts(String canonicalName, int skip, Integer size);

  @Query(
    " MATCH (target:User{canonicalName: $canonicalName}) " +
    " WITH target " +
    " CALL{ " +
    "   WITH target " +
    "   MATCH (target)-[:FOLLOWS]->(:User)-[:PUBLISH]->(p:Post{isPublic: true }) " +
    "   RETURN p" +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[:IS_CONNECTED]-(:User)-[:PUBLISH]->(p:Post) " +
    "   RETURN p" +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[:PUBLISH]->(p:Post) " +
    "   RETURN p" +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)-[:IS_INTERESTED]->(:Tag)<-[:HAS]-(p:Post{isPublic: true })<-[:PUBLISH]-(:User) " +
    "   RETURN p" +
    " UNION " +
    "   WITH target " +
    "   MATCH (target)<-[:MENTIONS]-(p:Post)<-[:PUBLISH]-(:User) " +
    "   RETURN p" +
    " } " +
    " RETURN COUNT(DISTINCT(p)) "
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
    " MATCH (c:Comment{id:$commentId}) " +
    " WITH c  " +
    " MATCH (u:User{canonicalName:$canonicalName})  " +
    " WITH c, u  " +
    " CALL {  " +
    "     WITH c, u  " +
    "     MATCH (c)-[]->(Post)<-[:PUBLISH]-(u) RETURN c as target " +
    "   UNION  " +
    "     WITH c, u  " +
    "     MATCH (c)<-[:WRITES]-(u) RETURN c as target " +
    " } DETACH DELETE target RETURN COUNT(target)>=1; "
  )
  Boolean removeComment(String canonicalName, String commentId);

  @Query("MATCH (p:Post{id: $postId}) WITH p MATCH (u:User{id: $userId}) MERGE (u)-[:PUBLISH]->(p);")
  void linkWithPublisher(String postId, String userId);

  @Query("MATCH (p:Post{id: $postId}) WITH p MATCH (u:User{id: $userId}) MERGE (p)-[:MENTIONS]->(u);")
  void mentionUser(String postId, String userId);

  @Query("MATCH (p:Post{id: $postId})-[r:IS_FAVORITE]-(u:User{canonicalName: $canonicalName}) return count(r)>0;")
  Boolean isFavorite(String postId, String canonicalName);

  @Query(
    " MATCH (tag:Tag{canonicalName: $tagCanonicalName}) " +
    " WITH tag " +
    " CALL {  " +
    "     WITH tag " +
    "     MATCH (:User{canonicalName: $requesterCanonicalName})-[:IS_CONNECTED]-(:User)-[:PUBLISH]->(p:Post{isPublic:false})-[rt:HAS]->(tag) " +
    "     RETURN p " +
    "   UNION " +
    "     WITH tag " +
    "     MATCH (tag)<-[:HAS]-(p:Post{isPublic:true})<-[:PUBLISH]-(:User) " +
    "     RETURN p " +
    " }  " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN COUNT(DISTINCT(p)) "
  )
  int countPostsByTag(String requesterCanonicalName, String tagCanonicalName);

  @Query(
    " MATCH (tag:Tag{canonicalName: $tagCanonicalName}) " +
    " WITH tag " +
    " MATCH (me:User{canonicalName: $requesterCanonicalName}) " +
    " WITH tag, me" +
    " CALL {  " +
    "     WITH tag, me " +
    "     MATCH (me)-[:IS_CONNECTED]-(u:User)-[r:PUBLISH]->(p:Post{isPublic:false})-[rt:HAS]->(tag) " +
    "     RETURN p,r,u" +
    "   UNION " +
    "     WITH tag " +
    "     MATCH (tag)<-[rt:HAS]-(p:Post{isPublic:true})<-[r:PUBLISH]-(u:User) " +
    "     RETURN p,r,u" +
    "   UNION " +
    "     WITH tag, me " +
    "     MATCH (tag)<-[rt:HAS]-(p:Post)<-[r:PUBLISH]-(me) " +
    "     RETURN p,r,me as u " +
    " }  " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t)  " +
    " ORDER BY p.createdAt DESC " +
    " SKIP $skip LIMIT $size "
  )
  Set<Post> getPostsByTag(String requesterCanonicalName, String tagCanonicalName, int skip, int size);

  @Query(
    " MATCH (u:User)-[r:PUBLISH]->(p:Post{id: $postId }) " +
    " OPTIONAL MATCH (p)-[rt:HAS]->(t:Tag) " +
    " OPTIONAL MATCH (p)-[rm:MENTIONS]->(m:User) " +
    " RETURN p, collect(r), collect(u), collect(rm), collect(m), collect(rt), collect(t) " +
    " LIMIT 1 "
  )
  Post getCreatedPost(String postId);

  @Query("MATCH (p:Post{id:$postId})-[h:HAS]->(:Tag) DELETE h")
  void removePostTags(String postId);

  @Query("MATCH (p:Post{id:$postId})-[m:MENTIONS]->(:User) DELETE m")
  void removePostMentions(String postId);

  @Query(
    "MATCH (p:Post{id: $postId}) WITH p MATCH (t:Tag{canonicalName: $tag}) MERGE (p)-[:HAS{displayName: $displayName}]->(t);"
  )
  void mentionTag(String postId, String tag, String displayName);

  @Query(
    "MATCH (p:Post{id:$postId})  SET p += {message: $newMessage, type: $newType, url: $newUrl, isPublic: $newIsPublic }"
  )
  void updatePost(String postId, String newMessage, Boolean newIsPublic, String newUrl, PostType newType);
}
