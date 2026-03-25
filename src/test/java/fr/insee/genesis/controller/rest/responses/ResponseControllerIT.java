package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResponseControllerIT extends IntegrationTestAbstract {

    @Nested
    @DisplayName("Get simplified responses tests")
    class GetSimplifiedResponsesTests {
        //HAPPY PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get simplified response, collected only")
        @SneakyThrows
        void get_simplified_response_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";
            String usualSurveyUnitId = "usualSurveyUnitId";
            Mode mode = Mode.WEB;
            RawResponseDto.QuestionnaireStateEnum questionnaireStateEnum =
                    RawResponseDto.QuestionnaireStateEnum.FINISHED;
            LocalDateTime validationDate = LocalDateTime.now().minusDays(2);
            DataState dataState = DataState.COLLECTED;

            SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
            surveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);
            surveyUnitDocument.setInterrogationId(interrogationId);
            surveyUnitDocument.setMode(mode.getModeName());
            surveyUnitDocument.setState(dataState.name());
            surveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
            surveyUnitDocument.setQuestionnaireState(questionnaireStateEnum);
            surveyUnitDocument.setValidationDate(validationDate);
            surveyUnitDocument.setCollectedVariables(new ArrayList<>());
            surveyUnitDocument.setExternalVariables(new ArrayList<>());

            String collectedVariableName = "var1";
            String collectedVariableValue = "value1";
            VariableDocument collectedVariableDocument = new VariableDocument();
            collectedVariableDocument.setVarId(collectedVariableName);
            collectedVariableDocument.setValue(collectedVariableValue);
            collectedVariableDocument.setIteration(1);
            collectedVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getCollectedVariables().add(collectedVariableDocument);

            String externalVariableName = "extvar1";
            String externalVariableValue = "extvalue1";
            VariableDocument externalVariableDocument = new VariableDocument();
            externalVariableDocument.setVarId(externalVariableName);
            externalVariableDocument.setValue(externalVariableValue);
            externalVariableDocument.setIteration(1);
            externalVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getExternalVariables().add(externalVariableDocument);

            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationIdAndCollectionInstrumentId(
                    interrogationId, collectionInstrumentId
            )).thenReturn(List.of(surveyUnitDocument));

            //WHEN + THEN
            mockMvc.perform(get("/responses/%s/%s/%s"
                            .formatted(collectionInstrumentId, mode.getModeName(), interrogationId)
                    )
                            .with(csrf()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.collectionInstrumentId").value(collectionInstrumentId))
                    .andExpect(jsonPath("$.interrogationId").value(interrogationId))
                    .andExpect(jsonPath("$.usualSurveyUnitId").value(usualSurveyUnitId))
                    .andExpect(jsonPath("$.mode").value(mode.getModeName()))
                    .andExpect(jsonPath("$.validationDate").value(validationDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                    .andExpect(jsonPath("$.questionnaireState").value(questionnaireStateEnum.name()))

                    .andExpect(jsonPath("$.variablesUpdate[0].varId").value(collectedVariableName))
                    .andExpect(jsonPath("$.variablesUpdate[0].value").value(collectedVariableValue))
                    .andExpect(jsonPath("$.variablesUpdate[0].state").value(dataState.name()))
                    .andExpect(jsonPath("$.variablesUpdate[0].scope").value(Constants.ROOT_GROUP_NAME))
                    .andExpect(jsonPath("$.variablesUpdate[0].iteration").value(1))

                    .andExpect(jsonPath("$.externalVariables[0].varId").value(externalVariableName))
                    .andExpect(jsonPath("$.externalVariables[0].value").value(externalVariableValue))
                    .andExpect(jsonPath("$.externalVariables[0].state").value(dataState.name()))
                    .andExpect(jsonPath("$.externalVariables[0].scope").value(Constants.ROOT_GROUP_NAME))
                    .andExpect(jsonPath("$.externalVariables[0].iteration").value(1));

        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get simplified response with an additionnal EDITED document")
        @SneakyThrows
        void get_simplified_response_with_edited_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";
            String usualSurveyUnitId = "usualSurveyUnitId";
            Mode mode = Mode.WEB;
            RawResponseDto.QuestionnaireStateEnum questionnaireStateEnum =
                    RawResponseDto.QuestionnaireStateEnum.FINISHED;
            LocalDateTime validationDate = LocalDateTime.now().minusDays(2);

            //Collected document
            SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
            surveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);
            surveyUnitDocument.setInterrogationId(interrogationId);
            surveyUnitDocument.setMode(mode.getModeName());
            surveyUnitDocument.setState(DataState.COLLECTED.name());
            surveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
            surveyUnitDocument.setQuestionnaireState(questionnaireStateEnum);
            surveyUnitDocument.setValidationDate(validationDate);
            surveyUnitDocument.setCollectedVariables(new ArrayList<>());
            surveyUnitDocument.setExternalVariables(new ArrayList<>());
            surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));

            String collectedVariableName = "var1";
            String collectedVariableValue = "value1";
            VariableDocument collectedVariableDocument = new VariableDocument();
            collectedVariableDocument.setVarId(collectedVariableName);
            collectedVariableDocument.setValue(collectedVariableValue);
            collectedVariableDocument.setIteration(1);
            collectedVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getCollectedVariables().add(collectedVariableDocument);

            String externalVariableName = "extvar1";
            String externalVariableValue = "extvalue1";
            VariableDocument externalVariableDocument = new VariableDocument();
            externalVariableDocument.setVarId(externalVariableName);
            externalVariableDocument.setValue(externalVariableValue);
            externalVariableDocument.setIteration(1);
            externalVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getExternalVariables().add(externalVariableDocument);

            //Edited document
            SurveyUnitDocument editedSurveyUnitDocument = new SurveyUnitDocument();
            editedSurveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);
            editedSurveyUnitDocument.setInterrogationId(interrogationId);
            editedSurveyUnitDocument.setMode(mode.getModeName());
            editedSurveyUnitDocument.setState(DataState.EDITED.name());
            editedSurveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
            editedSurveyUnitDocument.setQuestionnaireState(questionnaireStateEnum);
            editedSurveyUnitDocument.setValidationDate(validationDate);
            editedSurveyUnitDocument.setCollectedVariables(new ArrayList<>());
            editedSurveyUnitDocument.setExternalVariables(new ArrayList<>());
            editedSurveyUnitDocument.setRecordDate(LocalDateTime.now());

            String editedCollectedVariableValue = "editedvalue1";
            VariableDocument editedCollectedVariableDocument = new VariableDocument();
            editedCollectedVariableDocument.setVarId(collectedVariableName);
            editedCollectedVariableDocument.setValue(editedCollectedVariableValue);
            editedCollectedVariableDocument.setIteration(1);
            editedCollectedVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            editedSurveyUnitDocument.getCollectedVariables().add(editedCollectedVariableDocument);

            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationIdAndCollectionInstrumentId(
                    interrogationId, collectionInstrumentId
            )).thenReturn(List.of(surveyUnitDocument, editedSurveyUnitDocument));

            //WHEN + THEN
            mockMvc.perform(get("/responses/%s/%s/%s"
                            .formatted(collectionInstrumentId, mode.getModeName(), interrogationId)
                    )
                            .with(csrf()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.collectionInstrumentId").value(collectionInstrumentId))
                    .andExpect(jsonPath("$.interrogationId").value(interrogationId))
                    .andExpect(jsonPath("$.usualSurveyUnitId").value(usualSurveyUnitId))
                    .andExpect(jsonPath("$.mode").value(mode.getModeName()))
                    .andExpect(jsonPath("$.validationDate")
                            .value(validationDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                    .andExpect(jsonPath("$.questionnaireState").value(questionnaireStateEnum.name()))

                    .andExpect(jsonPath("$.variablesUpdate[0].varId").value(collectedVariableName))

                    //Collected variable should be from EDITED
                    .andExpect(jsonPath("$.variablesUpdate[0].value").value(editedCollectedVariableValue))
                    .andExpect(jsonPath("$.variablesUpdate[0].state").value(DataState.EDITED.name()))

                    .andExpect(jsonPath("$.variablesUpdate[0].scope").value(Constants.ROOT_GROUP_NAME))
                    .andExpect(jsonPath("$.variablesUpdate[0].iteration").value(1))

                    //External variable from COLLECTED (not present in EDITED)
                    .andExpect(jsonPath("$.externalVariables[0].varId").value(externalVariableName))
                    .andExpect(jsonPath("$.externalVariables[0].value").value(externalVariableValue))
                    .andExpect(jsonPath("$.externalVariables[0].state").value(DataState.COLLECTED.name()))
                    .andExpect(jsonPath("$.externalVariables[0].scope").value(Constants.ROOT_GROUP_NAME))
                    .andExpect(jsonPath("$.externalVariables[0].iteration").value(1));
        }

        //SAD PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get non existent simplified response")
        @SneakyThrows
        void get_simplified_response_not_found_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";
            Mode mode = Mode.WEB;

            //WHEN + THEN
            mockMvc.perform(get("/responses/%s/%s/%s"
                            .formatted(collectionInstrumentId, mode.getModeName(), interrogationId)
                    )
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get response latest states tests")
    class getLatestStatesTests {
        //HAPPY PATH
        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Get response latest states")
        @SneakyThrows
        void get_response_latest_states_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";
            String usualSurveyUnitId = "usualSurveyUnitId";
            Mode mode = Mode.WEB;
            LocalDateTime collectedDate = LocalDateTime.now().minusDays(1);
            LocalDateTime editedDate = LocalDateTime.now();


            //Context
            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocument.setWithReview(true);
            Mockito.when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(anyList()))
                    .thenReturn(List.of(dataProcessingContextDocument));

            //Collected document
            SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
            surveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);
            surveyUnitDocument.setInterrogationId(interrogationId);
            surveyUnitDocument.setMode(mode.getModeName());
            surveyUnitDocument.setState(DataState.COLLECTED.name());
            surveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
            surveyUnitDocument.setCollectedVariables(new ArrayList<>());
            surveyUnitDocument.setExternalVariables(new ArrayList<>());
            surveyUnitDocument.setRecordDate(collectedDate);

            String collectedVariableName = "var1";
            String collectedVariableValue = "value1";
            VariableDocument collectedVariableDocument = new VariableDocument();
            collectedVariableDocument.setVarId(collectedVariableName);
            collectedVariableDocument.setValue(collectedVariableValue);
            collectedVariableDocument.setIteration(1);
            collectedVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getCollectedVariables().add(collectedVariableDocument);

            String externalVariableName = "extvar1";
            String externalVariableValue = "extvalue1";
            VariableDocument externalVariableDocument = new VariableDocument();
            externalVariableDocument.setVarId(externalVariableName);
            externalVariableDocument.setValue(externalVariableValue);
            externalVariableDocument.setIteration(1);
            externalVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            surveyUnitDocument.getExternalVariables().add(externalVariableDocument);

            //Edited document
            SurveyUnitDocument editedSurveyUnitDocument = new SurveyUnitDocument();
            editedSurveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);
            editedSurveyUnitDocument.setInterrogationId(interrogationId);
            editedSurveyUnitDocument.setMode(mode.getModeName());
            editedSurveyUnitDocument.setState(DataState.EDITED.name());
            editedSurveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
            editedSurveyUnitDocument.setCollectedVariables(new ArrayList<>());
            editedSurveyUnitDocument.setExternalVariables(new ArrayList<>());
            editedSurveyUnitDocument.setRecordDate(editedDate);

            String editedCollectedVariableValue = "editedvalue1";
            VariableDocument editedCollectedVariableDocument = new VariableDocument();
            editedCollectedVariableDocument.setVarId(collectedVariableName);
            editedCollectedVariableDocument.setValue(editedCollectedVariableValue);
            editedCollectedVariableDocument.setIteration(1);
            editedCollectedVariableDocument.setScope(Constants.ROOT_GROUP_NAME);
            editedSurveyUnitDocument.getCollectedVariables().add(editedCollectedVariableDocument);

            //TODO use only one call to get context
            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationIdAndCollectionInstrumentId(
                    interrogationId, collectionInstrumentId
            )).thenReturn(List.of(surveyUnitDocument, editedSurveyUnitDocument));
            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationId(
                    interrogationId
            )).thenReturn(List.of(surveyUnitDocument, editedSurveyUnitDocument));

            //Metadata
            MetadataModel metadataModel = new MetadataModel();
            metadataModel.getVariables().putVariable(new Variable(
                    collectedVariableName,
                    metadataModel.getRootGroup(),
                    VariableType.STRING
            ));
            metadataModel.getVariables().putVariable(new Variable(
                    externalVariableName,
                    metadataModel.getRootGroup(),
                    VariableType.STRING
            ));
            QuestionnaireMetadataDocument questionnaireMetadataDocument = new QuestionnaireMetadataDocument(
                    collectionInstrumentId,
                    collectionInstrumentId,
                    mode,
                    metadataModel
            );
            Mockito.when(questionnaireMetadataMongoDBRepository
                            .findByCollectionInstrumentIdAndMode(collectionInstrumentId.toUpperCase(), mode))
                    .thenReturn(List.of(questionnaireMetadataDocument));

            //WHEN + THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .with(csrf())
                            .param("collectionInstrumentId", collectionInstrumentId)
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.collectedVariables[0].variableName").value(collectedVariableName))
                    .andExpect(jsonPath("$.collectedVariables[0].iteration").value(1))

                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[0].state")
                            .value(DataState.EDITED.name()))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[0].active")
                            .value(true))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[0].value")
                            .value(editedCollectedVariableValue))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[0].date")
                            .value(editedDate.format(DateTimeFormatter.ofPattern(Constants.VARIABLE_STATE_DTO_DATE_FORMAT))))

                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[1].state")
                            .value(DataState.COLLECTED.name()))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[1].active")
                            .value(false))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[1].value")
                            .value(collectedVariableValue))
                    .andExpect(jsonPath("$.collectedVariables[0].variableStates[1].date")
                            .value(collectedDate.format(DateTimeFormatter.ofPattern(Constants.VARIABLE_STATE_DTO_DATE_FORMAT))))


                    //External variable from COLLECTED (not present in EDITED)
                    .andExpect(jsonPath("$.externalVariables[0].variableName").value(externalVariableName))
                    .andExpect(jsonPath("$.externalVariables[0].iteration").value(1))
                    .andExpect(jsonPath("$.externalVariables[0].variableStates[0].state")
                            .value(DataState.COLLECTED.name()))
                    .andExpect(jsonPath("$.externalVariables[0].variableStates[0].active")
                            .value(true))
                    .andExpect(jsonPath("$.externalVariables[0].variableStates[0].value")
                            .value(externalVariableValue))
                    .andExpect(jsonPath("$.externalVariables[0].variableStates[0].date")
                            .value(collectedDate.format(DateTimeFormatter.ofPattern(Constants.VARIABLE_STATE_DTO_DATE_FORMAT))));
        }


        //SAD PATHS
        //FIXME remove ignore when multiple context case is implemented
        @Disabled("This needs to be implemented, code is actually using the first context")
        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Get response latest states should return 403 if at least one context with false withReview")
        @SneakyThrows
        void get_response_latest_states_multiple_contexts_one_false_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";


            //Contexts
            DataProcessingContextDocument dataProcessingContextDocumentTrue = new DataProcessingContextDocument();
            dataProcessingContextDocumentTrue.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocumentTrue.setWithReview(true);
            DataProcessingContextDocument dataProcessingContextDocumentFalse = new DataProcessingContextDocument();
            dataProcessingContextDocumentFalse.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocumentFalse.setWithReview(false);
            Mockito.when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(anyList()))
                    .thenReturn(List.of(dataProcessingContextDocumentTrue, dataProcessingContextDocumentFalse));

            //WHEN + THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .with(csrf())
                            .param("collectionInstrumentId", collectionInstrumentId)
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Get response latest states should return 500 if multiple contexts for one interrogationId")
        @SneakyThrows
        void get_response_latest_states_multiple_contexts_for_one_interrogation_test() {
            //GIVEN
            String collectionInstrumentId1 = "collectionInstrumentId1";
            String collectionInstrumentId2 = "collectionInstrumentId2";
            String interrogationId = "interrogationId";

            //Responses documents
            //(Get context uses get survey unit models by interrogationId to get collection instrument ids)
            SurveyUnitDocument surveyUnitDocument1 = new SurveyUnitDocument();
            surveyUnitDocument1.setCollectionInstrumentId(collectionInstrumentId1);
            surveyUnitDocument1.setInterrogationId(interrogationId);
            SurveyUnitDocument surveyUnitDocument2 = new SurveyUnitDocument();
            surveyUnitDocument2.setCollectionInstrumentId(collectionInstrumentId2);
            surveyUnitDocument2.setInterrogationId(interrogationId);
            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationId(interrogationId))
                    .thenReturn(List.of(surveyUnitDocument1, surveyUnitDocument2));

            //WHEN + THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .with(csrf())
                            .param("collectionInstrumentId", collectionInstrumentId1)
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isInternalServerError());
        }
    }
}