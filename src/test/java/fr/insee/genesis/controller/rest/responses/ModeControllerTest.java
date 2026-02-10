package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.exceptions.QuestionnaireNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModeControllerTest {
    @Mock
    private SurveyUnitApiPort surveyUnitApiPort;

    @InjectMocks
    private ModeController modeController;

    @Test
    void getModesByQuestionnaire() {
        //GIVEN
        List<Mode> modes = List.of(Mode.WEB, Mode.F2F);
        doReturn(modes).when(surveyUnitApiPort).findModesByCollectionInstrumentId(any());

        //WHEN
        ResponseEntity<List<Mode>> response = modeController.getModesByQuestionnaire(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);

        //THEN
        verify(surveyUnitApiPort, times(1)).findModesByCollectionInstrumentId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );
        Assertions.assertThat(response.getBody()).isEqualTo(modes);
    }

    @Test
    void getModesByQuestionnaire_not_found() {
        //GIVEN
        doThrow(QuestionnaireNotFoundException.class).when(surveyUnitApiPort).findModesByCollectionInstrumentId(any());

        //WHEN
        ResponseEntity<List<Mode>> response = modeController.getModesByQuestionnaire(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}