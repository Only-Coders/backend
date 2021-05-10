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
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.repository.CountryRepository;
import tech.onlycoders.backend.service.CountryService;

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
  CommandLineRunner runner(CountryService ser) {
    return args -> {
      var x = ser.findCountries("");
      System.out.println(x);
    };
  }
}
