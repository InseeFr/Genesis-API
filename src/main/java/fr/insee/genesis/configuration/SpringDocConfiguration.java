package fr.insee.genesis.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringDocConfiguration {

  @Value("${fr.insee.genesis.version}")
  private String projectVersion;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("Genesis API")
                    .description("Rest Endpoints and services to communicate with Genesis database")
                    .version(projectVersion)
            );
  }


}
