package tech.onlycoders.backend.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.joda.time.DateTime;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
public class GenericRepository {

  private final Neo4jClient neo4jClient;

  public GenericRepository(Neo4jClient neo4jClient) {
    this.neo4jClient = neo4jClient;
  }

  public Collection<Map<String, Object>> getPostsPerDay() {
    var date = new DateTime(new Date());
    date = date.minusMonths(3);

    var query =
      "MATCH (p:Post)\n" +
      "WHERE p.createdAt > " +
      date.toInstant().getMillis() +
      "\n" +
      "WITH apoc.date.format(p.createdAt, \"ms\", \"yyyy-MM-dd\") AS date,\n" +
      "COUNT(p) as found\n" +
      "RETURN date, found";
    return neo4jClient.query(query).fetch().all();
  }
}
