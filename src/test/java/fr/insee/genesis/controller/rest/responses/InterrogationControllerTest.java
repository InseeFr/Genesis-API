package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InterrogationControllerTest {
    private SurveyUnitApiPort surveyUnitApiPort;

    private InterrogationController interrogationController;

    @BeforeEach
    void setUp() {
        surveyUnitApiPort = mock(SurveyUnitApiPort.class);

        interrogationController = new InterrogationController(
                surveyUnitApiPort
        );
    }

    @Test
    void getAllInterrogationIdsByQuestionnaire_test() {
        //GIVEN
        List<InterrogationId> interrogationIds = List.of(
                new InterrogationId("test"),
                new InterrogationId("test2"));
        doReturn(interrogationIds).when(surveyUnitApiPort).findDistinctInterrogationIdsByQuestionnaireId(any());

        //WHEN
        ResponseEntity<List<InterrogationId>> response =
                interrogationController.getAllInterrogationIdsByQuestionnaire(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);

        //THEN
        verify(surveyUnitApiPort, times(1)).findDistinctInterrogationIdsByQuestionnaireId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );
        Assertions.assertThat(response.getBody()).isEqualTo(interrogationIds);
    }

    @Test
    void getAllInterrogationIdsByQuestionnaire_date_test() {
        //GIVEN
        LocalDateTime since = LocalDateTime.now();
        List<InterrogationId> interrogationIds = List.of(
                new InterrogationId("test"));
        doReturn(interrogationIds).when(surveyUnitApiPort).findDistinctInterrogationIdsByQuestionnaireIdAndDateAfter(
                any(),
                any()
        );


        //WHEN
        ResponseEntity<List<InterrogationId>> response =
            interrogationController.getAllInterrogationIdsByQuestionnaire(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    since
            );

        //THEN
        verify(surveyUnitApiPort, times(1)).findDistinctInterrogationIdsByQuestionnaireIdAndDateAfter(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                since
        );
        Assertions.assertThat(response.getBody()).isEqualTo(interrogationIds);
    }

    @Test
    void countAllInterrogationIdsByQuestionnaireOrCollectionInstrument() {
        //GIVEN
        long questionnaireCount = 2;
        long collectionInstrumentCount = 3;
        doReturn(questionnaireCount).when(surveyUnitApiPort).countResponsesByQuestionnaireId(any());
        doReturn(collectionInstrumentCount).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());

        //WHEN
        ResponseEntity<Long> response = interrogationController.countAllInterrogationIdsByQuestionnaireOrCollectionInstrument(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        verify(surveyUnitApiPort, times(1)).countResponsesByQuestionnaireId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );
        verify(surveyUnitApiPort, times(1)).countResponsesByCollectionInstrumentId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );
        Assertions.assertThat(response.getBody()).isEqualTo(collectionInstrumentCount + questionnaireCount);
    }

    @Test
    void getPaginatedInterrogationIdsByQuestionnaire_test() {
        //GIVEN
        List<InterrogationId> interrogationIds = List.of(
                new InterrogationId("test"),
                new InterrogationId("test2"));
        doReturn(interrogationIds).when(surveyUnitApiPort).findDistinctPageableInterrogationIdsByQuestionnaireId(
                any(), anyLong(), anyLong(), anyLong()
        );

        //WHEN
        ResponseEntity<List<InterrogationId>> response = interrogationController.getPaginatedInterrogationIdsByQuestionnaire(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                0,
                1000,
                0
        );

        //THEN
        verify(surveyUnitApiPort, times(1)).findDistinctPageableInterrogationIdsByQuestionnaireId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                0,
                1000,
                0
        );
        Assertions.assertThat(response.getBody()).isEqualTo(interrogationIds);
    }
}