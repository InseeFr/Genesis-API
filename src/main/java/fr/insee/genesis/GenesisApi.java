package fr.insee.genesis;

import fr.insee.genesis.configuration.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class GenesisApi {

        public static void main(String[] args) {
                try {
                        Class.forName("com.mongodb.internal.connection.InternalStreamConnection");
                } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                }
                configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
        }

        public static SpringApplicationBuilder configureApplicationBuilder(SpringApplicationBuilder springApplicationBuilder){
                return springApplicationBuilder.sources(GenesisApi.class)
                    .listeners(new PropertiesLogger());
        }

}
