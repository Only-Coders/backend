package tech.onlycoders.backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.PostRepository;

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
  CommandLineRunner runner(PersonRepository repo, PostRepository postRepo) {
    return args -> {
      var user = repo
        .findByEmail("marianoz@zoho.com")
        .orElseGet(
          () -> {
            var p = new Person();
            p.setEmail("marianoz@zoho.com");
            p.setCanonicalName("mariano-zunino");
            repo.save(p);
            return p;
          }
        );

      var role = new Role();
      role.setName("USER");
      user.setRole(role);
      repo.save(user);
    };
  }
}
