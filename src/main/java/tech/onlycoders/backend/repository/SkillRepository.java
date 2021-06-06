package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Skill;

@Repository
public interface SkillRepository extends Neo4jRepository<Skill, String> {
  Page<Skill> findByNameContainingIgnoreCase(String skillName, PageRequest pageRequest);

  @Query("MATCH (:User{canonicalName: $userCanonicalName})-[]->(s:Skill) RETURN s SKIP $skip LIMIT $size")
  List<Skill> getUserSkills(String userCanonicalName, Integer skip, Integer size);

  @Query("MATCH (:User{canonicalName: $userCanonicalName})-[]->(s:Skill) RETURN count(s)")
  int getUserSkillsQuantity(String userCanonicalName);

  @Query(
    " MATCH (s:Skill{canonicalName:$skillCN}) " +
    " WITH s  " +
    " MATCH (u:User{canonicalName:$canonicalName})  " +
    " WITH s, u  " +
    " MATCH (u)-[target:POSSESS]->(s) " +
    " DELETE target RETURN COUNT(target)>=1; "
  )
  Boolean deleteUserSkill(String skillCN, String canonicalName);
}
