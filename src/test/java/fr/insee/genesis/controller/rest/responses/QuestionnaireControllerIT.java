package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuestionnaireControllerIT extends IntegrationTestAbstract {
    @Nested
    @DisplayName("Get questionnaires tests")
    class GetQuestionnaireTests{
        //HAPPY PATH
        @Test
        @DisplayName("Get questionnaireId/collectionInstrumentId by interrogation test")
        @WithMockUser(roles = "READER")
        @SneakyThrows
        void get_collectionInstrumentId_by_interrogation_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId = "interrogationId";
            SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
            surveyUnitDocument.setInterrogationId(interrogationId);
            surveyUnitDocument.setCollectionInstrumentId(collectionInstrumentId);

            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationId(interrogationId))
                    .thenReturn(List.of(surveyUnitDocument));

            //WHEN
            MvcResult result = mockMvc.perform(get("/questionnaires/by-interrogation")
                            .with(csrf())
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isOk())
                    .andReturn();

            //THEN
            Assertions.assertThat(result.getResponse().getContentAsString()).isEqualTo(collectionInstrumentId);
        }

        //SAD PATHS
        @Test
        @DisplayName("Get non existent questionnaireId/collectionInstrumentId by interrogation test")
        @WithMockUser(roles = "READER")
        @SneakyThrows
        void get_collectionInstrumentId_by_interrogation_not_found_test(){
            //GIVEN
            String interrogationId = "interrogationId";

            //WHEN + THEN
            mockMvc.perform(get("/questionnaires/by-interrogation")
                            .with(csrf())
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Get multiple questionnaireId/collectionInstrumentId by interrogation test")
        @WithMockUser(roles = "READER")
        @SneakyThrows
        void get_collectionInstrumentId_by_interrogation_multiple_test(){
            //GIVEN
            String collectionInstrumentId1 = "collectionInstrumentId1";
            String collectionInstrumentId2 = "collectionInstrumentId2";
            String interrogationId = "interrogationId";

            SurveyUnitDocument surveyUnitDocument1 = new SurveyUnitDocument();
            surveyUnitDocument1.setInterrogationId(interrogationId);
            surveyUnitDocument1.setCollectionInstrumentId(collectionInstrumentId1);
            SurveyUnitDocument surveyUnitDocument2 = new SurveyUnitDocument();
            surveyUnitDocument2.setInterrogationId(interrogationId);
            surveyUnitDocument2.setCollectionInstrumentId(collectionInstrumentId2);

            Mockito.when(surveyUnitMongoDBRepository.findByInterrogationId(interrogationId))
                    .thenReturn(List.of(surveyUnitDocument1, surveyUnitDocument2));

            //WHEN + THEN
            mockMvc.perform(get("/questionnaires/by-interrogation")
                            .with(csrf())
                            .param("interrogationId", interrogationId))
                    .andExpect(status().isConflict());        }
    }
}