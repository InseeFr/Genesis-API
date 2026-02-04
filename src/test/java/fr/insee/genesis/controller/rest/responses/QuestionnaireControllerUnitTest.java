package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class QuestionnaireControllerUnitTest {

    private QuestionnaireController questionnaireController;
    private DataProcessingContextApiPort dataProcessingContextApiPort;

    @BeforeEach
    void setUp() {
        dataProcessingContextApiPort = mock(DataProcessingContextApiPort.class);
        questionnaireController = new QuestionnaireController(
                mock(SurveyUnitApiPort.class),
                dataProcessingContextApiPort
        );
    }

    @Test
    void getQuestionnairesWithReviewTest() {
        //WHEN
        questionnaireController.getQuestionnairesWithReview(true);

        verify(dataProcessingContextApiPort, times(1)).getCollectionInstrumentIds(anyBoolean());
    }
}