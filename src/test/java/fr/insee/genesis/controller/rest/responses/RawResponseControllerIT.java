package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static fr.insee.genesis.domain.utils.JsonUtils.jsonToMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RawResponseControllerIT extends IntegrationTestAbstract {

    @Autowired
    LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;

    @Autowired
    MongoTemplate mongoTemplate;


    @Nested
    @DisplayName("Raw data old model (lunatic-json) tests")
    class SaveLunaticJsonFromJsonBodyTests {
        @ParameterizedTest
        @ValueSource(strings = {"raw_data/rawdatasample.json", "raw_data/invalidData_but_correctJson.json"})
        @WithMockUser(roles = "COLLECT_PLATFORM")
        @DisplayName("Raw response saving with old model")
        @SneakyThrows
        void saveLunaticJson_test(String jsonFileName){
            //GIVEN
            String interrogationId = "INTERRO01";
            String questionnaireId = "QUEST01";

            //OK JSON body
            Path jsonFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, jsonFileName);
            String jsonBody = Files.readString(jsonFilePath);

            // WHEN
            mockMvc.perform(put("/responses/raw/lunatic-json/save")
                            .with(csrf())
                            .param("campaignName", "CAMPAIGN")
                            .param("questionnaireId", questionnaireId)
                            .param("interrogationId", interrogationId)
                            .param("mode", Mode.WEB.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isCreated());

            //THEN
            ArgumentCaptor<LunaticJsonRawDataDocument> lunaticJsonRawDataDocumentArgumentCaptor =
                    ArgumentCaptor.forClass(LunaticJsonRawDataDocument.class);
            verify(lunaticJsonMongoDBRepository,times(1)).insert(
                    lunaticJsonRawDataDocumentArgumentCaptor.capture()
            );
            LunaticJsonRawDataDocument lunaticJsonRawDataDocument = lunaticJsonRawDataDocumentArgumentCaptor.getValue();
            Assertions.assertThat(lunaticJsonRawDataDocument).isNotNull();
            Assertions.assertThat(lunaticJsonRawDataDocument.interrogationId()).isEqualTo(interrogationId);
            Assertions.assertThat(lunaticJsonRawDataDocument.questionnaireId()).isEqualTo(questionnaireId);
            Assertions.assertThat(lunaticJsonRawDataDocument.mode()).isEqualTo(Mode.WEB);

            //Same data than JSON
            Map<String, Object> jsonMap = jsonToMap(jsonBody);
            Assertions.assertThat(compareMaps(jsonMap, lunaticJsonRawDataDocument.data())).isTrue();
        }

        @Test
        @WithMockUser(roles = "COLLECT_PLATFORM")
        @DisplayName("Error 400 when invalid syntax")
        @SneakyThrows
        void saveLunaticJson_syntax_error_test(){
            //GIVEN
            String interrogationId = "INTERRO01";
            String questionnaireId = "QUEST01";

            //Bad JSON body
            Path jsonFilePath = Path.of(
                    TestConstants.TEST_RESOURCES_DIRECTORY,
                    "raw_data/rawdatasample_syntax_error.json"
            );
            String jsonBody = Files.readString(jsonFilePath);

            // WHEN
            mockMvc.perform(put("/responses/raw/lunatic-json/save")
                            .with(csrf())
                            .param("campaignName", "CAMPAIGN")
                            .param("questionnaireId", questionnaireId)
                            .param("interrogationId", interrogationId)
                            .param("mode", Mode.WEB.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isBadRequest());

            //THEN
            verifyNoInteractions(lunaticJsonMongoDBRepository);
        }
    }

    @Nested
    @DisplayName("Raw data filiere model (rawResponses) tests")
    class SaveRawResponsesFromJsonBodyTests {
        @Test
        @WithMockUser(roles = "COLLECT_PLATFORM")
        @DisplayName("Raw response saving with filiere model")
        @SneakyThrows
        void saveRawResponse_test(){
            //GIVEN
            //OK JSON
            Path jsonFilePath = Path.of(
                    TestConstants.TEST_RESOURCES_DIRECTORY,
                    "raw_data/rawdatasample_filieremodel.json"
            );
            String jsonBody = Files.readString(jsonFilePath);

            //WHEN
            mockMvc.perform(post("/raw-responses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isCreated());

            //THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> mapArgumentCaptor =
                    ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate, times(1)).save(
                    mapArgumentCaptor.capture(),
                    eq("rawResponses")
            );
            //Same data than input
            Map<String, Object> documentMap = mapArgumentCaptor.getValue();
            Assertions.assertThat(documentMap).isNotNull();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            JsonNode inputJson = objectMapper.readTree(jsonBody);
            JsonNode sentDocument = objectMapper.valueToTree(documentMap);

            Assertions.assertThat(sentDocument.get("payload").get("data").get(Constants.COLLECTED_NODE_NAME))
                    .isEqualTo(inputJson.get("data").get(Constants.COLLECTED_NODE_NAME));
            Assertions.assertThat(sentDocument.get("payload").get("data").get(Constants.EXTERNAL_NODE_NAME))
                    .isEqualTo(inputJson.get("data").get(Constants.EXTERNAL_NODE_NAME));
        }

        @ParameterizedTest
        @ValueSource(strings = {"raw_data/rawdatasample_syntax_error.json", "raw_data/rawdatasample.json"})
        @WithMockUser(roles = "COLLECT_PLATFORM")
        @DisplayName("Error 400 when invalid syntax or not on filiere model")
        @SneakyThrows
        void saveLunaticJson_syntax_or_json_schema_error_test(String jsonFilePathString){
            //GIVEN
            //Bad JSON body
            Path jsonFilePath = Path.of(
                    TestConstants.TEST_RESOURCES_DIRECTORY,
                    jsonFilePathString
            );
            String jsonBody = Files.readString(jsonFilePath);

            // WHEN
            mockMvc.perform(post("/raw-responses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isBadRequest());

            //THEN
            verifyNoInteractions(mongoTemplate);
        }
    }
}