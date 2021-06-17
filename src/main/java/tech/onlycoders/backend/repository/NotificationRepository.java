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
    "MATCH (n:NotificationConfig{id:$id})<-[r:CONFIGURES]-(u:User{canonicalName:$canonicalName})" +
    "RETURN n, collect(r), collect(u)"
  )
  Optional<NotificationConfig> findById(String id, String canonicalName);

  @Query(
    "MATCH (n:NotificationConfig)<-[r:CONFIGURES]-(u:User{canonicalName:$canonicalName}) WHERE n.type <> 'NEW_ADMIN_ACCOUNT' RETURN n "
  )
  List<NotificationConfig> getUserNotificationConfig(String canonicalName);
}
