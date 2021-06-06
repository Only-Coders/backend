package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.WorkPosition;

@Repository
public interface WorkPositionRepository extends Neo4jRepository<WorkPosition, String> {
  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:WorkPosition{id: $workPositionId}) CREATE (a)-[:WORKS]->(b)")
  void addUserWorkPosition(String workPositionId, String userId);

  @Query(
    "MATCH (u:User{canonicalName: $canonicalName})-[:WORKS]->(p:WorkPosition)-[o:ON]->(w) " +
    "WHERE NOT EXISTS(p.until) RETURN p, collect(o), collect(w) ORDER BY p.since DESC"
  )
  List<WorkPosition> getUserCurrentPositions(String canonicalName);

  @Query(
    "MATCH (u:User{canonicalName: $canonicalName})-[:WORKS]->(p:WorkPosition)-[o:ON]->(w) " +
    "WHERE NOT EXISTS(p.until) RETURN p, collect(o), collect(w) ORDER BY p.since DESC limit 1;"
  )
  Optional<WorkPosition> getUserCurrentPosition(String canonicalName);

  @Query(
    "MATCH (p:Workplace)<-[o:ON]-(w:WorkPosition)<-[r:WORKS]-(u:User{canonicalName: $canonicalName}) return w, collect(r), collect(o)" +
    " ,  collect(p) SKIP $skip LIMIT $size;"
  )
  List<WorkPosition> getUserJobs(String canonicalName, Integer skip, Integer size);

  @Query(
    "MATCH (p:Workplace)<-[o:ON]-(w:WorkPosition)<-[r:WORKS]-(u:User{canonicalName: $canonicalName}) return count(w)"
  )
  Integer countUserJobs(String canonicalName);

  @Query("MATCH (w:WorkPosition{id: $workPositionId})<-[:WORKS]-(:User{email: $email}) return count(w) > 0")
  boolean isOwner(String email, String workPositionId);

  @Query("MATCH (w:WorkPosition{id: $workPositionId}) DETACH DELETE w")
  void remove(String workPositionId);
}
