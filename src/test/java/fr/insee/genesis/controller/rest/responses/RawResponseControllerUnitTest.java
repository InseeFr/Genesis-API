package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class RawResponseControllerUnitTest {

    static RawResponseController rawResponseController;
    static LunaticJsonRawDataApiPort lunaticJsonRawDataStub;

    @BeforeEach
    void init(){
        lunaticJsonRawDataStub = mock(LunaticJsonRawDataApiPort.class);
        rawResponseController = new RawResponseController(
            lunaticJsonRawDataStub,
                mock(RawResponseApiPort.class),
                mock(RawResponseInputRepository.class)
        );
    }


    @Test
    void getUnprocessedJsonRawDataQuestionnairesIds_test(){
        // GIVEN
        Set<String> questionnaireIds = new HashSet<>();
        questionnaireIds.add("QUEST1");
        questionnaireIds.add("QUEST2");
        doReturn(questionnaireIds).when(lunaticJsonRawDataStub).getUnprocessedDataQuestionnaireIds();

        // WHEN
        ResponseEntity<Set<String>> response = rawResponseController.getUnprocessedJsonRawDataQuestionnairesIds();

        // THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().containsExactlyInAnyOrder("QUEST1","QUEST2");
    }

}
