package tech.onlycoders.backend.bean.neo4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.mapping.callback.BeforeBindCallback;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.utils.CanonicalFactory;

@Configuration
public class PreSaveHook {

  @Bean
  BeforeBindCallback<Person> setPersonCanonicalName() {
    return entity -> {
      if (needsCanonicalName(entity)) {
        var randomChars = RandomStringUtils.randomAlphabetic(5);
        var canonicalName = CanonicalFactory.getCanonicalName(
          entity.getFirstName() + entity.getLastName() + randomChars
        );
        entity.setCanonicalName(canonicalName);
      }
      return entity;
    };
  }

  private boolean needsCanonicalName(Person entity) {
    return (
      entity.getUpdatedAt().compareTo(entity.getCreatedAt()) != 0 ||
      entity.getCanonicalName() == null ||
      entity.getCanonicalName().isBlank()
    );
  }
}
