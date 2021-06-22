package tech.onlycoders.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Person;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
  Optional<Person> findByEmail(String email);

  @Query(
    " MATCH (u:Person)-[h:HAS]-(r:Role) WHERE u.fullName =~ $likeName RETURN u, collect(h), collect(r) " +
    " ORDER BY u[$sortBy] DESC SKIP $skip LIMIT $size"
  )
  List<Person> paginateAllPeople(String likeName, String sortBy, Integer skip, Integer size);

  @Query(
    " MATCH (u:Person)-[h:HAS]-(r:Role{name: 'ADMIN'}) WHERE u.fullName =~ $likeName RETURN u, collect(h), collect(r) " +
    " ORDER BY u.id DESC SKIP $skip LIMIT $size"
  )
  List<Person> paginateAllAdmins(String likeName, Integer skip, Integer size);

  @Query(
    " MATCH (u:Person)-[h:HAS]-(r:Role{name: 'USER'}) WHERE u.fullName =~ $likeName RETURN u, collect(h), collect(r) " +
    " ORDER BY u[$sortBy] DESC SKIP $skip LIMIT $size"
  )
  List<Person> paginateAllUsers(String likeName, String sortBy, Integer skip, Integer size);

  @Query("MATCH (u:Person) WHERE u.fullName =~ $likeName RETURN count(u)")
  int countAllPeople(String likeName);

  @Query("MATCH (u:Admin) WHERE u.fullName =~ $likeName RETURN count(u)")
  int countAllAdmins(String likeName);

  @Query("MATCH (u:User) WHERE u.fullName =~ $likeName RETURN count(u)")
  int countAllUsers(String likeName);
}
