package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.CombinedRawData;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CombinedRawDataServiceTest {

    @Mock
    LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Mock
    RawResponsePersistencePort rawResponsePersistencePort;

    @InjectMocks
    CombinedRawDataService combinedRawDataService;

    @Test
    void getCombinedRawDataByInterrogationId_shouldReturnCombinedData() {
        // GIVEN
        String interrogationId = "INTERROGATION_1";

        // Mock des RawResponses
        RawResponse response1 = new RawResponse(
                new ObjectId(),
                interrogationId,
                "COLLECTION_1",
                Mode.WEB,
                Map.of("question1", "answer1"),
                LocalDateTime.now(),
                null
        );

        RawResponse response2 = new RawResponse(
                new ObjectId(),
                interrogationId,
                "COLLECTION_1",
                Mode.WEB,
                Map.of("question2", "answer2"),
                LocalDateTime.now(),
                null
        );

        List<RawResponse> rawResponses = List.of(response1, response2);

        // Mock des LunaticJsonRawData
        LunaticJsonRawDataModel lunatic1 = LunaticJsonRawDataModel.builder()
                .campaignId("CAMPAIGN")
                .questionnaireId("QUESTIONNAIRE")
                .interrogationId(interrogationId)
                .data(Map.of("key", "value"))
                .mode(Mode.WEB)
                .build();
        List<LunaticJsonRawDataModel> lunaticRawData = List.of(lunatic1);

        // WHEN
        Mockito.when(rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId))
                .thenReturn(rawResponses);

        Mockito.when(lunaticJsonRawDataPersistencePort.findRawDataByInterrogationID(interrogationId))
                .thenReturn(lunaticRawData);

        CombinedRawData result = combinedRawDataService.getCombinedRawDataByInterrogationId(interrogationId);

        // THEN - assertions
        Assertions.assertThat(result.rawResponses()).hasSize(2).containsExactlyInAnyOrderElementsOf(rawResponses);
        Assertions.assertThat(result.lunaticRawData()).hasSize(1).containsExactlyInAnyOrderElementsOf(lunaticRawData);
    }

    @Test
    void getCombinedRawData_shouldHandleEmptyLists() {
        String interrogationId = "INTERROGATION_EMPTY";

        Mockito.when(rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId))
                .thenReturn(List.of());
        Mockito.when(lunaticJsonRawDataPersistencePort.findRawDataByInterrogationID(interrogationId))
                .thenReturn(List.of());
        CombinedRawData result = combinedRawDataService.getCombinedRawDataByInterrogationId(interrogationId);

        Assertions.assertThat(result.rawResponses()).isEmpty();
        Assertions.assertThat(result.lunaticRawData()).isEmpty();
    }

}
