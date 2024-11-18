package fr.insee.genesis.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

  @Value("${fr.insee.genesis.version}")
  private String projectVersion;

  public static final String BEARERSCHEME = "bearerAuth";

  @Bean
  @ConditionalOnProperty(name = "fr.insee.genesis.authentication", havingValue = "NONE")
  public OpenAPI noAuthOpenAPI() {
    return generateOpenAPI();
  }

  @Bean
  @ConditionalOnProperty(name = "fr.insee.genesis.authentication", havingValue = "OIDC")
  public OpenAPI oidcOpenAPI() {
    return generateOpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(BEARERSCHEME))
            .components(
                    new Components()
                            .addSecuritySchemes(BEARERSCHEME,
                                    new SecurityScheme()
                                            .name(BEARERSCHEME)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                            )
            );
  }

  private OpenAPI generateOpenAPI(){
    return new OpenAPI()
            .addServersItem(new Server().url("/"))
            .info(new Info()
                    .title("Genesis API")
                    .description("Rest Endpoints and services to communicate with Genesis database")
                    .version(projectVersion)
            );
  }


}
