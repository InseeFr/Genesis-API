package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.insee.genesis.domain.utils.JsonUtils.jsonToMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class RawResponseControllerIT extends IntegrationTestAbstract {

    @Autowired
    LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    ObjectMapper objectMapper;

    @BeforeEach
    void init(){
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }


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

    @Nested
    @DisplayName("Raw data processing tests")
    class rawDataProcessingTests {

        @Test
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Filiere model raw data should be processed with a interrogationId list")
        @SneakyThrows
        void process_raw_response_interrogation_list_test(){
            //GIVEN
            String collectionInstrumentId = "TESTQUEST";
            Mode mode = Mode.WEB;
            List<String> interrogationIds = List.of("INTERRO1", "INTERRO2");

            String variableName = "VAR1";
            String collectedValue = "value1";
            Map<String, String> collectedVariablesAndValues = new HashMap<>();
            collectedVariablesAndValues.put(variableName, collectedValue);

            String externalVariableName = "EXTVAR1";
            String externalValue = "externalvalue1";
            Map<String, String> externalVariablesAndValues = new HashMap<>();
            externalVariablesAndValues.put(externalVariableName, externalValue);

            setFiliereModelTestMockBehaviour(
                    collectionInstrumentId,
                    mode,
                    interrogationIds,
                    collectedVariablesAndValues,
                    externalVariablesAndValues
            );

            // WHEN
            mockMvc.perform(post("/raw-responses/process")
                            .with(csrf())
                            .param("collectionInstrumentId", collectionInstrumentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(interrogationIds)))
                    .andExpect(status().isOk());

            //THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SurveyUnitDocument>> listArgumentCaptor =
                    ArgumentCaptor.forClass(List.class);
            verify(surveyUnitMongoDBRepository, times(1))
                    .insert(listArgumentCaptor.capture());

            //Check saved data
            List<SurveyUnitDocument> savedDocuments = listArgumentCaptor.getValue();
            Assertions.assertThat(savedDocuments).isNotNull().hasSize(interrogationIds.size());
            for(String interrogationId : interrogationIds){
                SurveyUnitDocument savedDocument = savedDocuments.stream().filter(
                        surveyUnitDocument ->
                                surveyUnitDocument.getInterrogationId().equals(interrogationId)
                ).toList().getFirst();
                Assertions.assertThat(savedDocument.getCollectedVariables()).isNotNull().hasSize(1);
                VariableDocument variableDocument = savedDocument.getCollectedVariables().getFirst();
                Assertions.assertThat(variableDocument.getVarId()).isEqualTo(variableName);
                Assertions.assertThat(variableDocument.getValue()).isEqualTo(interrogationId + collectedValue);
                Assertions.assertThat(variableDocument.getScope()).isEqualTo(Constants.ROOT_GROUP_NAME);
                Assertions.assertThat(variableDocument.getIteration()).isEqualTo(1);
                Assertions.assertThat(variableDocument.getParentId()).isNull();

                Assertions.assertThat(savedDocument.getExternalVariables()).isNotNull().hasSize(1);
                variableDocument = savedDocument.getExternalVariables().getFirst();
                Assertions.assertThat(variableDocument.getVarId()).isEqualTo(externalVariableName);
                Assertions.assertThat(variableDocument.getValue()).isEqualTo(interrogationId + externalValue);
                Assertions.assertThat(variableDocument.getScope()).isEqualTo(Constants.ROOT_GROUP_NAME);
                Assertions.assertThat(variableDocument.getIteration()).isEqualTo(1);
                Assertions.assertThat(variableDocument.getParentId()).isNull();
            }
        }

//        @Test
//        @WithMockUser(roles = "SCHEDULER")
//        @DisplayName("Filiere model raw data should be processed with a interrogationId list")
//        @SneakyThrows
//        void process_raw_response_collectionInstrumentId_test(){
//            //GIVEN
//
//            // WHEN
//            mockMvc.perform(post("/raw-responses/%s/process".formatted(collectionInstrumentId))
//                            .with(csrf())
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk());
//
//            //THEN
//            @SuppressWarnings("unchecked")
//            ArgumentCaptor<List<SurveyUnitDocument>> listArgumentCaptor =
//                    ArgumentCaptor.forClass(List.class);
//            verify(surveyUnitMongoDBRepository, times(1))
//                    .insert(listArgumentCaptor.capture());
//
//            //Check saved data
//            //INTERRO1
//            List<SurveyUnitDocument> savedDocuments = listArgumentCaptor.getValue();
//            Assertions.assertThat(savedDocuments).isNotNull().hasSize(2);
//
//            SurveyUnitDocument savedDocument = savedDocuments.stream().filter(
//                    surveyUnitDocument ->
//                            surveyUnitDocument.getInterrogationId().equals(interrogationIds.getFirst())
//            ).toList().getFirst();
//            Assertions.assertThat(savedDocument.getCollectedVariables()).isNotNull().hasSize(1);
//            VariableDocument variableDocument = savedDocument.getCollectedVariables().getFirst();
//            Assertions.assertThat(variableDocument.getVarId()).isEqualTo(variableName);
//            Assertions.assertThat(variableDocument.getValue()).isEqualTo(value1);
//            Assertions.assertThat(variableDocument.getScope()).isEqualTo(metadataModel.getRootGroup().getName());
//            Assertions.assertThat(variableDocument.getIteration()).isEqualTo(1);
//            Assertions.assertThat(variableDocument.getParentId()).isNull();
//
//            //INTERRO2
//            savedDocument = savedDocuments.stream().filter(
//                    surveyUnitDocument ->
//                            surveyUnitDocument.getInterrogationId().equals(interrogationIds.getLast())
//            ).toList().getFirst();
//            Assertions.assertThat(savedDocument.getCollectedVariables()).isNotNull().hasSize(1);
//            variableDocument = savedDocument.getCollectedVariables().getFirst();
//            Assertions.assertThat(variableDocument.getVarId()).isEqualTo(variableName);
//            Assertions.assertThat(variableDocument.getValue()).isEqualTo(value2);
//            Assertions.assertThat(variableDocument.getScope()).isEqualTo(metadataModel.getRootGroup().getName());
//            Assertions.assertThat(variableDocument.getIteration()).isEqualTo(1);
//            Assertions.assertThat(variableDocument.getParentId()).isNull();
//        }

        //OLD RAW DOC TODO met ça dans le bon test
//            String oldVariableValue = "value1";
//            Map<String, Object> dataMap = getNewDataMap();
//            addVariableToDataMap(dataMap, oldVariableName, oldVariableValue);
//            LunaticJsonRawDataDocument lunaticJsonRawDataDocument = LunaticJsonRawDataDocument.builder()
//                    .interrogationId("INTERRO1")
//                    .questionnaireId(collectionInstrumentId)
//                    .idUE("UE1")
//                    .mode(mode)
//                    .data(dataMap)
//                    .recordDate(LocalDateTime.now())
//                    .build();

        @SneakyThrows
        private void setFiliereModelTestMockBehaviour(
                String collectionInstrumentId,
                Mode mode,
                List<String> interrogationIds,
                Map<String, String> collectedVariableNamesAndValues,
                Map<String, String> externalVariableNamesAndValues
                ){
            //Disable withReview
            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocument.setWithReview(false);
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(anyList()))
                    .thenReturn(List.of(dataProcessingContextDocument));

            //Mode list
            when(controllerUtils.getModesList(eq(collectionInstrumentId), any()))
                    .thenReturn(List.of(mode));

            //Metadata
            MetadataModel metadataModel = new MetadataModel();
            for(Map.Entry<String,String> variable : collectedVariableNamesAndValues.entrySet()) {
                metadataModel.getVariables().putVariable(
                        new Variable(
                                variable.getKey(),
                                metadataModel.getRootGroup(),
                                VariableType.STRING
                        )
                );
            }
            for(Map.Entry<String,String> variable : externalVariableNamesAndValues.entrySet()) {
                metadataModel.getVariables().putVariable(
                        new Variable(
                                variable.getKey(),
                                metadataModel.getRootGroup(),
                                VariableType.STRING
                        )
                );
            }
            QuestionnaireMetadataDocument questionnaireMetadataDocument = new QuestionnaireMetadataDocument(
                    null,
                    collectionInstrumentId,
                    mode,
                    metadataModel
            );
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(
                    collectionInstrumentId, mode
            )).thenReturn(List.of(questionnaireMetadataDocument));

            //FILIERE RAW DOCS
            List<RawResponseDocument> rawResponseDocuments = new ArrayList<>();
            for(String interrogationId : interrogationIds){
                Map<String, Object> dataMap = getNewDataMap();
                //COLLECTED VARIABLES
                for(Map.Entry<String,String> variable : collectedVariableNamesAndValues.entrySet()) {
                    String variableName = variable.getKey();
                    //To have different value on each interrogation
                    String value = interrogationId + variable.getValue();

                    addVariableToDataMap(dataMap, variableName, value, Constants.COLLECTED_NODE_NAME);
                }
                //EXTERNAL VARIABLES
                for(Map.Entry<String,String> variable : externalVariableNamesAndValues.entrySet()) {
                    String variableName = variable.getKey();
                    String value = interrogationId + variable.getValue();

                    addVariableToDataMap(dataMap, variableName, value, Constants.EXTERNAL_NODE_NAME);
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("data", dataMap);

                //Filiere model fields
                payload.put("usualSurveyUnitId", "usualSurveyUnitId");
                payload.put("majorModelVersion", "1");
                payload.put("questionnaireState", RawResponseDto.QuestionnaireStateEnum.FINISHED.toString());

                RawResponseDocument rawResponseDocument = RawResponseDocument.builder()
                        .interrogationId(interrogationId)
                        .collectionInstrumentId(collectionInstrumentId)
                        .mode(mode.getJsonName())
                        .payload(payload)
                        .recordDate(LocalDateTime.now())
                        .build();
                rawResponseDocuments.add(rawResponseDocument);
            }
            when(rawResponseRepository.findByCollectionInstrumentIdAndModeAndInterrogationIdList(
                    collectionInstrumentId,
                    mode.getJsonName(),
                    interrogationIds
            )).thenReturn(rawResponseDocuments);
        }

        private Map<String, Object> getNewDataMap() {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(Constants.COLLECTED_NODE_NAME, new HashMap<>());
            dataMap.put(Constants.EXTERNAL_NODE_NAME, new HashMap<>());
            return dataMap;
        }

        private void addVariableToDataMap(Map<String, Object> dataMap,
                                          String variableName,
                                          String value,
                                          String collectedOrExternal
        ) {
            switch (collectedOrExternal){
                case Constants.COLLECTED_NODE_NAME -> addCollectedVariableToDataMap(
                        dataMap, variableName, value
                );
                case Constants.EXTERNAL_NODE_NAME -> addExternalVariableToDataMap(
                        dataMap, variableName, value
                );
                case null -> log.warn("null collectedOrExternal");
                default -> log.warn("Unknown collectedOrExternal : {}", collectedOrExternal);
            }

        }

        private void addCollectedVariableToDataMap(Map<String, Object> dataMap,
                                          String variableName,
                                          String value
        ) {
            @SuppressWarnings("unchecked")
            Map<String, Object> collectedMap = (Map<String, Object>) dataMap.get(Constants.COLLECTED_NODE_NAME);
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put(Constants.COLLECTED_NODE_NAME, value);
            collectedMap.put(variableName, variableMap);
        }
        private void addExternalVariableToDataMap(Map<String, Object> dataMap,
                                                   String variableName,
                                                   String value
        ) {
            @SuppressWarnings("unchecked")
            Map<String, Object> externalMap = (Map<String, Object>) dataMap.get(Constants.EXTERNAL_NODE_NAME);
            externalMap.put(variableName, value);
        }
    }
}