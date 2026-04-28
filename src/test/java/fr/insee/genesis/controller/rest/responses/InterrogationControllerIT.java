package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitInterrogationProjection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InterrogationControllerIT extends IntegrationTestAbstract {

    @Nested
    @DisplayName("GET collection-instruments/{collectionInstrumentId}/interrogations tests")
    class GetAllInterrogationIdsByQuestionnaireTests {
        @Test
        @DisplayName("Should return 200 with all interrogationIds if no since nor until")
        @WithMockUser(roles = "USER_KRAFTWERK")
        @SneakyThrows
        void getAllInterrogationIdsByQuestionnaire_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId1 = "interrogationId1";
            String interrogationId2 = "interrogationId2";
            Instant recordDate = Instant.now();

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection1 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection1.getInterrogationId()).thenReturn(interrogationId1);
            when(surveyUnitInterrogationProjection1.getRecordDate()).thenReturn(recordDate);

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection2 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection2.getInterrogationId()).thenReturn(interrogationId2);
            when(surveyUnitInterrogationProjection2.getRecordDate()).thenReturn(recordDate);

            List<SurveyUnitInterrogationProjection> surveyUnitInterrogationProjectionList =
                    List.of(surveyUnitInterrogationProjection1,
                    surveyUnitInterrogationProjection2);

            when(surveyUnitMongoDBRepository.findProjectedByCollectionInstrumentId(collectionInstrumentId))
                    .thenReturn(surveyUnitInterrogationProjectionList);

            //WHEN + THEN
            mockMvc.perform(get(("/collection-instruments/%s/interrogations").formatted(collectionInstrumentId))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(interrogationId1)))
                    .andExpect(content().string(containsString(interrogationId2)));
        }

        @Test
        @DisplayName("Should return 200 with interrogationIds if only until")
        @WithMockUser(roles = "USER_KRAFTWERK")
        @SneakyThrows
        void getAllInterrogationIdsByQuestionnaire_no_since_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId1 = "interrogationId1";
            String interrogationId2 = "interrogationId2";
            LocalDateTime until = LocalDateTime.now().minusDays(1);

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection1 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection1.getInterrogationId()).thenReturn(interrogationId1);
            when(surveyUnitInterrogationProjection1.getRecordDate()).thenReturn(until.minusDays(1).toInstant(ZoneOffset.UTC));

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection2 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection2.getInterrogationId()).thenReturn(interrogationId2);
            when(surveyUnitInterrogationProjection2.getRecordDate()).thenReturn(until.minusDays(2).toInstant(ZoneOffset.UTC));

            List<SurveyUnitInterrogationProjection> surveyUnitInterrogationProjectionList =
                    List.of(surveyUnitInterrogationProjection1,
                            surveyUnitInterrogationProjection2);

            when(surveyUnitMongoDBRepository.findProjectedByCollectionInstrumentIdAndUntil(
                    collectionInstrumentId, until.atZone(ZoneId.systemDefault()).toInstant()
            )).thenReturn(surveyUnitInterrogationProjectionList);

            //WHEN + THEN
            mockMvc.perform(get(("/collection-instruments/%s/interrogations").formatted(collectionInstrumentId))
                            .with(csrf())
                            .param("until", until.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(interrogationId1)))
                    .andExpect(content().string(containsString(interrogationId2)));
        }

        @Test
        @DisplayName("Should return 200 with interrogationIds if only since")
        @WithMockUser(roles = "USER_KRAFTWERK")
        @SneakyThrows
        void getAllInterrogationIdsByQuestionnaire_no_until_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId1 = "interrogationId1";
            String interrogationId2 = "interrogationId2";
            LocalDateTime since = LocalDateTime.now().minusDays(3);

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection1 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection1.getInterrogationId()).thenReturn(interrogationId1);
            when(surveyUnitInterrogationProjection1.getRecordDate()).thenReturn(since.plusDays(1).toInstant(ZoneOffset.UTC));

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection2 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection2.getInterrogationId()).thenReturn(interrogationId2);
            when(surveyUnitInterrogationProjection2.getRecordDate()).thenReturn(since.plusDays(2).toInstant(ZoneOffset.UTC));

            List<SurveyUnitInterrogationProjection> surveyUnitInterrogationProjectionList =
                    List.of(surveyUnitInterrogationProjection1,
                            surveyUnitInterrogationProjection2);

            when(surveyUnitMongoDBRepository.findProjectedByCollectionInstrumentIdAndSince(
                    collectionInstrumentId, since.atZone(ZoneId.systemDefault()).toInstant()
            )).thenReturn(surveyUnitInterrogationProjectionList);

            //WHEN + THEN
            mockMvc.perform(get(("/collection-instruments/%s/interrogations").formatted(collectionInstrumentId))
                            .with(csrf())
                            .param("since", since.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(interrogationId1)))
                    .andExpect(content().string(containsString(interrogationId2)));
        }

        @Test
        @DisplayName("Should return 200 with interrogationIds between if until and since are present")
        @WithMockUser(roles = "USER_KRAFTWERK")
        @SneakyThrows
        void getAllInterrogationIdsByQuestionnaire_until_and_since_test() {
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            String interrogationId1 = "interrogationId1";
            String interrogationId2 = "interrogationId2";
            LocalDateTime since = LocalDateTime.now().minusDays(5);
            LocalDateTime until = LocalDateTime.now().minusDays(1);

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection1 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection1.getInterrogationId()).thenReturn(interrogationId1);
            when(surveyUnitInterrogationProjection1.getRecordDate()).thenReturn(since.plusDays(2).toInstant(ZoneOffset.UTC));

            SurveyUnitInterrogationProjection surveyUnitInterrogationProjection2 =
                    mock(SurveyUnitInterrogationProjection.class);
            when(surveyUnitInterrogationProjection2.getInterrogationId()).thenReturn(interrogationId2);
            when(surveyUnitInterrogationProjection2.getRecordDate()).thenReturn(since.plusDays(3).toInstant(ZoneOffset.UTC));

            List<SurveyUnitInterrogationProjection> surveyUnitInterrogationProjectionList =
                    List.of(surveyUnitInterrogationProjection1,
                            surveyUnitInterrogationProjection2);

            when(surveyUnitMongoDBRepository.findProjectedByCollectionInstrumentIdAndBetween(
                    collectionInstrumentId,
                    since.atZone(ZoneId.systemDefault()).toInstant(),
                    until.atZone(ZoneId.systemDefault()).toInstant()
            )).thenReturn(surveyUnitInterrogationProjectionList);

            //WHEN + THEN
            mockMvc.perform(get(("/collection-instruments/%s/interrogations").formatted(collectionInstrumentId))
                            .with(csrf())
                            .param("until", until.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT))
                            .param("since", since.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(interrogationId1)))
                    .andExpect(content().string(containsString(interrogationId2)));
        }
    }
}