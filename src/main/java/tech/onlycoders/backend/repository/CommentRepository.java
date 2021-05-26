package tech.onlycoders.backend.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tech.onlycoders.backend.model.Comment;
import tech.onlycoders.backend.model.ContactRequest;

@Repository
public interface CommentRepository extends Neo4jRepository<Comment, String> {}
