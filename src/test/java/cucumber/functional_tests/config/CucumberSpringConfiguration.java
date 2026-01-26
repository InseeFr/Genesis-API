package cucumber.functional_tests.config;

import fr.insee.genesis.GenesisApi;
import fr.insee.genesis.infrastructure.repository.ContextualExternalVariableMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.ContextualPreviousVariableMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LastJsonExtractionMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.QuestionnaireMetadataMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(classes = GenesisApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-cucumber")
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, DataMongoAutoConfiguration.class})
@AutoConfigureTestRestTemplate
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
    private LunaticModelMongoDBRepository lunaticModelMongoDBRepository;
    @MockitoBean
    private LastJsonExtractionMongoDBRepository lastJsonExtractionMongoDBRepository;
    @MockitoBean
    private DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;
    @MockitoBean
    private ContextualPreviousVariableMongoDBRepository contextualPreviousVariableMongoDBRepository;
    @MockitoBean
    private ContextualExternalVariableMongoDBRepository contextualExternalVariableMongoDBRepository;
    @MockitoBean
    private QuestionnaireMetadataMongoDBRepository questionnaireMetadataMongoDBRepository;
}

