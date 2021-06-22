package tech.onlycoders.backend.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.dto.admin.response.ReadGenericUserDto;
import tech.onlycoders.backend.dto.admin.response.ReadRoleDto;

@Repository
public class GenericRepository {

  private final Neo4jClient neo4jClient;

  public GenericRepository(Neo4jClient neo4jClient) {
    this.neo4jClient = neo4jClient;
  }

  private static ReadGenericUserDto ReadGenericUserDtoMapper(TypeSystem typeSystem, Record record) {
    var user = record.get("u").asMap();
    var firstName = (String) user.get("firstName");
    var userRole = (String) user.get("role");
    var lastName = (String) user.get("lastName");
    var canonicalName = (String) user.get("canonicalName");
    var imageURI = (String) user.get("imageURI");
    var blocked = (Boolean) user.getOrDefault("blocked", false);
    return ReadGenericUserDto
      .builder()
      .firstName(firstName)
      .lastName(lastName)
      .canonicalName(canonicalName)
      .blocked(blocked)
      .role(ReadRoleDto.builder().name(userRole).build())
      .imageURI(imageURI)
      .build();
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

  public Collection<Map<String, Object>> getPostsPerHour() {
    var date = new DateTime(new Date());
    date = date.minusDays(30);

    var query =
      "MATCH (p:Post)\n" +
      "WHERE p.createdAt > " +
      date.toInstant().getMillis() +
      "\n" +
      "WITH apoc.date.format(p.createdAt, \"ms\", \"HH:'00'\") AS hour,\n" +
      "COUNT(p) as found\n" +
      "RETURN hour, found\n" +
      "ORDER BY hour";
    return neo4jClient.query(query).fetch().all();
  }

  public Collection<Map<String, Object>> getReactionsPerHour() {
    var date = new DateTime(new Date());
    date = date.minusDays(30);

    var query =
      "MATCH (p:Reaction)\n" +
      "WHERE p.createdAt > " +
      date.toInstant().getMillis() +
      "\n" +
      "WITH apoc.date.format(p.createdAt, \"ms\", \"HH:'00'\") AS hour,\n" +
      "COUNT(p) as found\n" +
      "RETURN hour, found\n" +
      "ORDER BY hour";
    return neo4jClient.query(query).fetch().all();
  }

  public Collection<ReadGenericUserDto> paginateAllPeople(
    String likeName,
    String sortBy,
    String orderBy,
    String role,
    Integer skip,
    Integer size
  ) {
    var query = String.format(
      " MATCH (u:Person)-[h:HAS]-(r:Role) WHERE u.fullName =~ \"%s\" AND r.name =~ \"%s\" " +
      " RETURN u{.*, role: r.name} " +
      " ORDER BY u[\"%s\"] %s SKIP %d LIMIT %d",
      likeName,
      role,
      sortBy,
      orderBy,
      skip,
      size
    );
    return neo4jClient
      .query(query)
      .fetchAs(ReadGenericUserDto.class)
      .mappedBy(GenericRepository::ReadGenericUserDtoMapper)
      .all();
  }
}
