package tech.onlycoders.backend.bean;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.onlycoders.backend.dto.ApiErrorResponse;

@Configuration
@OpenAPIDefinition(info = @Info(title = "OnlyCoders API", version = "v1"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {

  @Bean
  public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
    var schema = ModelConverters
      .getInstance()
      .resolveAsResolvedSchema(new AnnotatedType(ApiErrorResponse.class))
      .schema;
    return openApi ->
      openApi
        .getPaths()
        .values()
        .forEach(
          pathItem ->
            pathItem
              .readOperations()
              .forEach(
                operation -> {
                  ApiResponses apiResponses = operation.getResponses();
                  apiResponses.remove("400");
                  ApiResponse apiResponse = new ApiResponse()
                    .content(
                      new Content()
                        .addMediaType(
                          org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                          new MediaType().schema(schema)
                        )
                    );
                  apiResponses.addApiResponse("4xx", apiResponse);
                  apiResponses.addApiResponse("5xx", apiResponse);
                }
              )
        );
  }
}
