package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Degree;

@Repository
public interface DegreeRepository extends Neo4jRepository<Degree, String> {
  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:Degree{id: $degreeId}) CREATE (a)-[:STUDIES]->(b)")
  void storeDegree(String degreeId, String userId);

  @Query(
    " MATCH (i:Institute)<-[a:AT]-(d:Degree)<-[]-(u:User{canonicalName: $canonicalName}) " +
    " RETURN d, COLLECT(a), COLLECT(i) " +
    " SKIP $skip LIMIT $size;"
  )
  List<Degree> getUserDegrees(String canonicalName, Integer skip, Integer size);

  @Query(" MATCH (i:Institute)<-[]-()<-[]-(u:User{canonicalName: $canonicalName}) RETURN COUNT(i)")
  Integer countUserDegrees(String canonicalName);
}
