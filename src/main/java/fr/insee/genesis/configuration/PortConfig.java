package fr.insee.genesis.configuration;

import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.service.SurveyUnitImpl;
import fr.insee.genesis.infrastructure.adapter.SurveyUnitMongoAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortConfig {

    @Bean
    SurveyUnitPersistencePort surveyUnitPersistence(){
        return new SurveyUnitMongoAdapter();
    }

    @Bean
    SurveyUnitApiPort surveyUnitService(){
        return new SurveyUnitImpl(surveyUnitPersistence());
    }

}
