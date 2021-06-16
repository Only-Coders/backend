package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.NotificationConfig;

@Repository
public interface NotificationRepository extends Neo4jRepository<NotificationConfig, String> {
  @Query(
    "MATCH (n:NotificationConfig{type:$type}) <-[r:CONFIGURES]-(u:User{canonicalName:$canonicalName})" +
    " return n, collect(r), collect(u)"
  )
  Optional<NotificationConfig> findByType(String type, String canonicalName);

  @Query(
    "MATCH (n:NotificationConfig{id:$id}) WITH n MATCH (u:User{canonicalName:$canonicalName})" +
    " CREATE (n)<-[r:CONFIGURES]-(u)"
  )
  void createConfiguration(String id, String canonicalName);

  @Query("MATCH (n:NotificationConfig)<-[r:CONFIGURES]-(u:User{canonicalName:$canonicalName}) RETURN n")
  List<NotificationConfig> getUserNotificationConfig(String canonicalName);
}
