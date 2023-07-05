package fr.insee.genesis.configuration;

import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.domain.service.SurveyUnitUpdateImpl;
import fr.insee.genesis.infrastructure.adapter.SurveyUnitUpdateJpaAdapter;
import fr.insee.genesis.infrastructure.adapter.SurveyUnitUpdateMongoAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortConfig {

    @Value("${fr.insee.genesis.persistence.implementation}")
    private String persistenceImplementation;

    @Bean
    SurveyUnitUpdatePersistencePort surveyUnitUpdatePersistence(){
        if (persistenceImplementation.equals("postgresql")) {
            return new SurveyUnitUpdateJpaAdapter();
        }
        if (persistenceImplementation.equals("mongodb")) {
            return new SurveyUnitUpdateMongoAdapter();
        }
        return null;
    }

    @Bean
    SurveyUnitUpdateApiPort surveyUnitService(){
        return new SurveyUnitUpdateImpl(surveyUnitUpdatePersistence());
    }

}
