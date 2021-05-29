package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.ReportType;

@Repository
public interface ReportTypeRepository extends Neo4jRepository<ReportType, String> {}
