package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.BlackList;

@Repository
public interface BlacklistRepository extends Neo4jRepository<BlackList, String> {
  @Query("MATCH (b:BlackList) WHERE b.email =~ $likeEmail RETURN b ORDER BY b.id DESC SKIP $skip LIMIT $size")
  List<BlackList> paginateAllBlackListedUsers(String likeEmail, Integer skip, Integer size);

  @Query("MATCH (b:BlackList) WHERE b.email =~ $likeEmail RETURN COUNT(b)")
  Integer countAllBlackListedUsers(String likeEmail);

  @Query("MATCH (b:BlackList) WHERE b.email = $email DELETE b")
  void removeByEmail(String email);
}
