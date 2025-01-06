package fr.insee.genesis.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfiguration extends AbstractMongoClientConfiguration {
    @Override
    @NonNull
    protected String getDatabaseName() {
        return "CollectedDataRepository";
    }

    @Override
    public boolean autoIndexCreation() {
        return true;
    }
}
