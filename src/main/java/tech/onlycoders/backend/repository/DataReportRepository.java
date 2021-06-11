package tech.onlycoders.backend.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.BaseEntity;
import tech.onlycoders.backend.model.Comment;
import tech.onlycoders.backend.repository.projections.PartialComment;

@Repository
public interface DataReportRepository extends Neo4jRepository<BaseEntity, String> {
  @Query("MATCH (b:BlackList) RETURN count(b)")
  Integer getBlacklistedUsersQuantity();

  @Query("MATCH (u:User{blocked:true}) RETURN count(u)")
  Integer getBlockedUsersQuantity();

  @Query("MATCH (u:User{blocked:false}) RETURN count(u)")
  Integer getUsersQuantity();
}
