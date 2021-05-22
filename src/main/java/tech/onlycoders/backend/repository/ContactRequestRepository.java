package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.ContactRequest;

@Repository
public interface ContactRequestRepository extends Neo4jRepository<ContactRequest, String> {
  @Query("MATCH (a:User{id: $userId}) WITH a MATCH (b:ContactRequest{id: $contactRequestId}) MERGE (a)-[:SENDS]->(b)")
  void createSendContactRequest(String contactRequestId, String userId);

  @Query("MATCH (:User{id: $sourceId})-[]->(cr:ContactRequest)-[]->(target:User{id: $targetId}) detach delete cr")
  void deleteRequest(String sourceId, String targetId);

  @Query("MATCH (u:User)-[]->(cr:ContactRequest)-[]->(:User{email: $email}) RETURN count(cr)")
  int getReceivedContactRequestTotalQuantity(String email);

  @Query(
    "MATCH (u:User)-[r]->(cr:ContactRequest)-[]->(:User{email: $email}) " +
    "RETURN cr, collect(r), collect(u) SKIP $skip LIMIT $size"
  )
  List<ContactRequest> getReceivedContactRequests(String email, Integer skip, Integer size);

  @Query("MATCH (:User{id: $sourceId})-[]->(a:ContactRequest)-[]->(:User{id: $targetId}) RETURN count(a)>0")
  boolean hasPendingRequest(String sourceId, String targetId);
}
