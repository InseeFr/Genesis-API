package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RawResponseControllerUnitTest {

    private static RawResponseController rawResponseController;

    private static LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    private static RawResponseApiPort rawResponseApiPort;
    private static RawResponseInputRepository rawResponseInputRepository;

    @BeforeEach
    void init(){
        lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        rawResponseApiPort = mock(RawResponseApiPort.class);
        rawResponseInputRepository = mock(RawResponseInputRepository.class);

        rawResponseController = new RawResponseController(
                lunaticJsonRawDataApiPort,
                rawResponseApiPort,
                rawResponseInputRepository
        );
    }

    @Test
    void saveRawResponsesFromJsonBody_test() {
        //WHEN
        ResponseEntity<String> response = rawResponseController.saveRawResponsesFromJsonBody(
                null,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                TestConstants.DEFAULT_INTERROGATION_ID,
                null,
                null,
                new HashMap<>()
        );

        //THEN
        verify(lunaticJsonRawDataApiPort, times(1)).save(any());
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void saveRawResponsesFromJsonBody_exception_test() {
        //GIVEN
        doThrow(ArrayIndexOutOfBoundsException.class).when(lunaticJsonRawDataApiPort).save(any());

        //WHEN
        ResponseEntity<String> response = rawResponseController.saveRawResponsesFromJsonBody(
                null,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                TestConstants.DEFAULT_INTERROGATION_ID,
                null,
                null,
                new HashMap<>()
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void saveRawResponsesFromRawResponseDto_test() {
        //WHEN
        ResponseEntity<String> response = rawResponseController.saveRawResponsesFromRawResponseDto(
                new RawResponseDto()
        );

        //THEN
        verify(rawResponseInputRepository, times(1)).saveAsRawJson(any());
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    @SneakyThrows
    void processRawResponses_test() {
        //GIVEN
        int datacount = 50;
        int formattedDatacount = 25;
        DataProcessResult dataProcessResult = new DataProcessResult(
                datacount,
                formattedDatacount,
                new ArrayList<>()
        );
        doReturn(dataProcessResult).when(rawResponseApiPort).processRawResponses(any(), any(), any());

        //WHEN
        rawResponseController.processRawResponses(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, new ArrayList<>());

        //THEN
        verify(rawResponseApiPort, times(1)).processRawResponses(
                eq(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID),
                any(),
                any()
        );
    }

    @Test
    @SneakyThrows
    void processRawResponsesByCollectionInstrumentId_test() {
        //GIVEN
        int datacount = 50;
        int formattedDatacount = 25;
        DataProcessResult dataProcessResult = new DataProcessResult(
                datacount,
                formattedDatacount,
                new ArrayList<>()
        );
        doReturn(dataProcessResult).when(rawResponseApiPort).processRawResponses(any());

        //WHEN
        rawResponseController.processRawResponsesByCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);

        //THEN
        verify(rawResponseApiPort, times(1)).processRawResponses(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );
    }

    @Test
    @SneakyThrows
    void getUnprocessedCollectionInstrument_test() {
        //GIVEN
        List<String> expected = List.of("test", "test2");
        doReturn(expected).when(rawResponseApiPort).getUnprocessedCollectionInstrumentIds();

        //WHEN
        ResponseEntity<List<String>> response = rawResponseController.getUnprocessedCollectionInstrument();

        //THEN
        verify(rawResponseApiPort, times(1)).getUnprocessedCollectionInstrumentIds();
        Assertions.assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getUnprocessedJsonRawDataQuestionnairesIds_test(){
        // GIVEN
        Set<String> questionnaireIds = new HashSet<>();
        questionnaireIds.add("QUEST1");
        questionnaireIds.add("QUEST2");
        doReturn(questionnaireIds).when(lunaticJsonRawDataApiPort).getUnprocessedDataQuestionnaireIds();

        // WHEN
        ResponseEntity<Set<String>> response = rawResponseController.getUnprocessedJsonRawDataQuestionnairesIds();

        // THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().containsExactlyInAnyOrder("QUEST1","QUEST2");
    }

}
