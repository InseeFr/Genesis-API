package fr.insee.genesis;

import fr.insee.genesis.configuration.MinioConfig;
import fr.insee.genesis.configuration.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
@EnableConfigurationProperties(MinioConfig.class)
public class GenesisApi {

        public static void main(String[] args) {
                configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);        }

        public static SpringApplicationBuilder configureApplicationBuilder(SpringApplicationBuilder springApplicationBuilder){
                return springApplicationBuilder.sources(GenesisApi.class)
                    .listeners(new PropertiesLogger());
        }

}
