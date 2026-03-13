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
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
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

import java.util.List;
import java.util.Map;

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

    /**
     * Compare 2 maps
     * @return true of those maps have the same keys and values
     */
    protected <K, V> boolean compareMaps(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == map2) return true;
        if (map1 == null || map2 == null) return false;
        if (!map1.keySet().equals(map2.keySet())) return false;

        for (Map.Entry<K, V> entry : map1.entrySet()) {
            Object val1 = entry.getValue();
            Object val2 = map2.get(entry.getKey());

            if (!deepEquals(val1, val2)) return false;
        }

        return true;
    }

    /**
     * Check if two objects are equals in their content (elements of list, key/values of map...)
     * @return true if identical
     */
    @SuppressWarnings("unchecked")
    private boolean deepEquals(Object val1, Object val2) {
        if (val1 == val2) return true;
        if (val1 == null || val2 == null) return false;

        if (val1 instanceof Map && val2 instanceof Map) {
            return compareMaps((Map<String, Object>) val1, (Map<String, Object>) val2);
        }

        if (val1 instanceof List && val2 instanceof List) {
            List<Object> list1 = (List<Object>) val1;
            List<Object> list2 = (List<Object>) val2;
            if (list1.size() != list2.size()) return false;
            for (int i = 0; i < list1.size(); i++) {
                if (!deepEquals(list1.get(i), list2.get(i))) return false;
            }
            return true;
        }

        if (val1 instanceof Number && val2 instanceof Number) {
            return ((Number) val1).doubleValue() == ((Number) val2).doubleValue();
        }

        return val1.equals(val2);
    }
}
