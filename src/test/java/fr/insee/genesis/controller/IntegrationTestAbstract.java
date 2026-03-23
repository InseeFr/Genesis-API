package fr.insee.genesis.controller;

import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.infrastructure.repository.ContextualExternalVariableMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.ContextualPreviousVariableMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LastJsonExtractionMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.QuestionnaireMetadataMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.RawResponseRepository;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * All integration tests must inherit this class
 * It mocks all repositories and adds them to spring context
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "logging.level.=DEBUG"
})
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public abstract class IntegrationTestAbstract {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ControllerUtils controllerUtils;
    @MockitoBean
    protected MongoTemplate mongoTemplate;
    @MockitoBean
    protected RawResponseRepository rawResponseRepository;
    @MockitoBean
    protected SurveyUnitMongoDBRepository surveyUnitMongoDBRepository;
    @MockitoBean
    protected LastJsonExtractionMongoDBRepository lastJsonExtractionMongoDBRepository;
    @MockitoBean
    protected LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;
    @MockitoBean
    protected RundeckExecutionDBRepository rundeckExecutionDBRepository;
    @MockitoBean
    protected DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;
    @MockitoBean
    protected LunaticModelMongoDBRepository lunaticModelMongoDBRepository;
    @MockitoBean
    protected ContextualPreviousVariableMongoDBRepository contextualPreviousVariableMongoDBRepository;
    @MockitoBean
    protected ContextualExternalVariableMongoDBRepository contextualExternalVariableMongoDBRepository;
    @MockitoBean
    protected QuestionnaireMetadataMongoDBRepository questionnaireMetadataMongoDBRepository;

    //Mocked quality tool port
    @MockitoBean
    protected SurveyUnitQualityToolPort surveyUnitQualityToolPort;
}
