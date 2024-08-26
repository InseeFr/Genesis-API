package fr.insee.genesis.configuration;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.service.ScheduleService;
import fr.insee.genesis.domain.service.SurveyUnitService;
import fr.insee.genesis.infrastructure.adapter.ScheduleMongoAdapter;
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
        return new SurveyUnitService(surveyUnitPersistence());
    }


    @Bean
    SchedulePersistencePort schedulePersistence(){
        return new ScheduleMongoAdapter();
    }
    @Bean
    ScheduleApiPort scheduleService(){return new ScheduleService(schedulePersistence());}
}
