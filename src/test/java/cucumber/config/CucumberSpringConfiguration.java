package cucumber.config;

import fr.insee.genesis.GenesisApi;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.VariableTypeMongoDBRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = GenesisApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ImportAutoConfiguration(exclude = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
public class CucumberSpringConfiguration {
    @MockitoBean
    private MongoTemplate mongoTemplate;
    @MockitoBean
    private SurveyUnitMongoDBRepository surveyUnitMongoDBRepository;
    @MockitoBean
    private LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;
    @MockitoBean
    private RundeckExecutionDBRepository rundeckExecutionDBRepository;
    @MockitoBean
    private ScheduleMongoDBRepository scheduleMongoDBRepository;
    @MockitoBean
    private VariableTypeMongoDBRepository variableTypeMongoDBRepository;
}

