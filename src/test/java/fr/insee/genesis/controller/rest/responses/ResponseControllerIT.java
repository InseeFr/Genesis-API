package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResponseControllerIT extends IntegrationTestAbstract {

    @Nested
    @DisplayName("Get simplified responses tests")
    class GetSimplifiedResponsesTests{
        //HAPPY PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get simplified response, collected only")
        @SneakyThrows
        void get_simplified_response_test(){
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
                    .with(csrf())
                    .param("interrogationId", interrogationId))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.collectionInstrumentId").value(collectionInstrumentId))
                    .andExpect(jsonPath("$.interrogationId").value(interrogationId))
                    .andExpect(jsonPath("$.usualSurveyUnitId").value(usualSurveyUnitId))
                    .andExpect(jsonPath("$.mode").value(mode.getModeName()))
                    .andExpect(jsonPath("$.validationDate").value(validationDate.toString()))
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
    }

    @Nested
    @DisplayName("Get response latest states tests")
    class getLatestStatesTests{
        //TODO
    }


}