package tech.onlycoders.backend;

import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.model.Country;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.CountryRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.TagRepository;

@SpringBootApplication
@OpenAPIDefinition(
  info = @Info(
    title = "Sample Spring Boot API",
    version = "v1",
    description = "A demo project using Spring Boot with Swagger-UI enabled"
  )
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableNeo4jAuditing
public class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

  @Bean
  CommandLineRunner runner(
    FirebaseService firebaseService,
    TagRepository tagRepository,
    PersonRepository personRepository,
    CountryRepository countryRepository
  ) {
    //    Faker faker = new Faker();
    //    List<Tag> tags = tagRepository.findAll();
    //    List<Country> countries = countryRepository.findAll();
    //    for (var i = 0; i < 30; i++) {
    //      var person = new User();
    //      person.setBlocked(false);
    //      person.setFirstName(faker.name().firstName());
    //      person.setLastName(faker.name().lastName());
    //      person.setEmail(person.getFirstName().toLowerCase() + person.getLastName().toLowerCase() + "@fakemail.com");
    //      person.setCanonicalName(person.getFirstName().toLowerCase() + "-" + person.getLastName().toLowerCase() + "000");
    //      person.getTags().add(tags.get(faker.number().numberBetween(0, tags.size() - 1)));
    //      person.setCountry(countries.get(faker.number().numberBetween(0, 2)));
    //      System.out.println(person);
    //      personRepository.save(person);
    //    }

    return args -> {};
  }
}
