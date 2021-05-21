package tech.onlycoders.backend.bean;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.core.mapping.callback.BeforeBindCallback;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.utils.CanonicalFactory;

@Configuration
@EnableNeo4jAuditing
@EnableTransactionManagement
public class Neo4jConfig {

  @Bean
  BeforeBindCallback<Person> setPersonCanonicalName() {
    return entity -> {
      if (needsCanonicalName(entity)) {
        var randomChars = RandomStringUtils.randomAlphabetic(5);
        var canonicalName = CanonicalFactory.getCanonicalName(
          entity.getFirstName() + entity.getLastName() + "-" + randomChars
        );
        entity.setCanonicalName(canonicalName);
      }
      return entity;
    };
  }

  private boolean needsCanonicalName(Person entity) {
    return (
      entity.getCreatedAt().compareTo(entity.getUpdatedAt()) == 0 ||
      entity.getCanonicalName() == null ||
      entity.getCanonicalName().isBlank()
    );
  }
}
