package tech.onlycoders.backend.repository;

import java.util.Collection;
import java.util.Map;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
public class GenericRepository {

  private final Neo4jClient neo4jClient;

  public GenericRepository(Neo4jClient neo4jClient) {
    this.neo4jClient = neo4jClient;
  }

  public Collection<Map<String, Object>> getPostsPerDay() {
    var query =
      "MATCH (p:Post)\n" +
      "WHERE p.createdAt > 1623713625000\n" +
      "WITH apoc.date.format(p.createdAt, \"ms\", \"yyyy-MM-dd\") AS date,\n" +
      "COUNT(p) as found\n" +
      "RETURN date, found";
    return neo4jClient.query(query).fetch().all();
  }
}
