package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.ContactRequest;

@Repository
public interface ContactRequestRepository extends Neo4jRepository<ContactRequest, String> {
  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:ContactRequest{id: $contactRequestId}) MERGE (a)-[:SENDS]->(b)")
  void storeContactRequest(String contactRequestId, String userId);

  @Query("MATCH (:User{id: $sourceId})-[]->(cr:ContactRequest)-[]->(target:User{id: $targetId}) detach delete cr")
  void deleteRequest(String sourceId, String targetId);

  @Query("MATCH (:User{id: $sourceId})-[]->(a:ContactRequest)-[]->(:User{id: $targetId}) RETURN count(a)>0")
  boolean hasPendingRequest(String sourceId, String targetId);
}
