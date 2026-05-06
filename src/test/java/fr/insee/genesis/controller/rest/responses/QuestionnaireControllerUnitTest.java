package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionnaireControllerUnitTest {
    @Mock
    private DataProcessingContextApiPort dataProcessingContextApiPort;

    @Mock
    private SurveyUnitApiPort surveyUnitApiPort;

    @InjectMocks
    private QuestionnaireController questionnaireController;

    @Test
    void getQuestionnaires_test() {
        //GIVEN
        Set<String> questionnaires = Set.of("test", "test2");
        doReturn(questionnaires).when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();

        //WHEN
        ResponseEntity<Set<String>> response = questionnaireController.getQuestionnaires();

        //THEN
        verify(surveyUnitApiPort, times(1))
                .findDistinctQuestionnairesAndCollectionInstrumentIds();
        Assertions.assertThat(response.getBody()).isEqualTo(questionnaires);
    }

    @Test
    void getQuestionnairesWithReview_test() {
        //GIVEN
        List<String> questionnaires = List.of("test");
        doReturn(questionnaires).when(dataProcessingContextApiPort).getCollectionInstrumentIds(anyBoolean());

        //WHEN
        ResponseEntity<List<String>> response = questionnaireController.getQuestionnairesWithReview(true);

        //THEN
        verify(dataProcessingContextApiPort, times(1)).getCollectionInstrumentIds(true);
        Assertions.assertThat(response.getBody()).isEqualTo(questionnaires);
    }

    @Test
    @SneakyThrows
    void getQuestionnaireByInterrogation_test() {
        //GIVEN
        String questionnaireId = "test";
        doReturn(questionnaireId).when(surveyUnitApiPort)
                .findQuestionnaireIdByInterrogationId(any());

        //WHEN
        ResponseEntity<String> response = questionnaireController.getQuestionnaireByInterrogation(TestConstants.DEFAULT_INTERROGATION_ID);

        //THEN
        verify(surveyUnitApiPort, times(1)).findQuestionnaireIdByInterrogationId(
                TestConstants.DEFAULT_INTERROGATION_ID
        );
        Assertions.assertThat(response.getBody()).isEqualTo(questionnaireId);
    }
}