package tech.onlycoders.backend.repository;

import java.util.List;
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
    "WHERE NOT EXISTS(p.until) RETURN p, collect(o), collect(w)  ORDER BY p.since DESC"
  )
  List<WorkPosition> getUserCurrentPositions(String canonicalName);
}
