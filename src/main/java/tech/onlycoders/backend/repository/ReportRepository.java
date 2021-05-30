package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Report;

@Repository
public interface ReportRepository extends Neo4jRepository<Report, String> {
  @Query("MATCH (p:Post{id: $postId}) with p MATCH (r:Report{id: $id}) CREATE (r)-[:FOR]->(p)")
  void linkReportToPost(String postId, String id);

  @Query(
    "MATCH (u:User{canonicalName: $canonicalName}) with u MATCH (r:Report{id: $id})" + "CREATE (u)-[:CREATES]->(r)"
  )
  void linkReportToUser(String canonicalName, String id);
}
