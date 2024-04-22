package fr.insee.genesis.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

  @Value("${fr.insee.genesis.version}")
  private String projectVersion;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .addServersItem(new Server().url("/"))
            .info(new Info()
                    .title("Genesis API")
                    .description("Rest Endpoints and services to communicate with Genesis database")
                    .version(projectVersion)
            );
  }


}
