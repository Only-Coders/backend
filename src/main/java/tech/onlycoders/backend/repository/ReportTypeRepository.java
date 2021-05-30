package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.ReportType;

@Repository
public interface ReportTypeRepository extends Neo4jRepository<ReportType, String> {
  @Query("MATCH (t:ReportType{language: $language}) RETURN t")
  List<ReportType> findAllByLanguage(String language);
}
