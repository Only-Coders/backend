package tech.onlycoders.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.model.Workplace;

@Repository
public interface WorkPositionRepository extends Neo4jRepository<WorkPosition, String> {
  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:WorkPosition{id: $workPositionId}) CREATE (a)-[:WORKS]->(b)")
  void storeWorkPosition(String workPositionId, String userId);
}
