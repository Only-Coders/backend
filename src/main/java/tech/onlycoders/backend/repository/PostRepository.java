package tech.onlycoders.backend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Post;

@Repository
public interface PostRepository extends Neo4jRepository<Post, String> {}
