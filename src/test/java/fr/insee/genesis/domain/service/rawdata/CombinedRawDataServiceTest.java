package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
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
        RawResponseModel response1 = new RawResponseModel(
                new ObjectId(),
                interrogationId,
                "COLLECTION_1",
                Mode.WEB,
                Map.of("question1", "answer1"),
                LocalDateTime.now(),
                null
        );
        RawResponseModel response2 = new RawResponseModel(
                new ObjectId(),
                interrogationId,
                "COLLECTION_1",
                Mode.WEB,
                Map.of("question2", "answer2"),
                LocalDateTime.now(),
                null
        );
        List<RawResponseModel> rawResponseModels = List.of(response1, response2);
        Mockito.when(rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId))
                .thenReturn(rawResponseModels);

        LunaticJsonRawDataModel lunatic1 = LunaticJsonRawDataModel.builder()
                .campaignId("CAMPAIGN")
                .questionnaireId("QUESTIONNAIRE")
                .interrogationId(interrogationId)
                .data(Map.of("key", "value"))
                .mode(Mode.WEB)
                .build();
        List<LunaticJsonRawDataModel> lunaticRawData = List.of(lunatic1);
        Mockito.when(lunaticJsonRawDataPersistencePort.findRawDataByInterrogationId(interrogationId))
                .thenReturn(lunaticRawData);

        // WHEN
        CombinedRawDataDto result = combinedRawDataService.getCombinedRawDataByInterrogationId(interrogationId);

        // THEN - assertions
        Assertions.assertThat(result.rawResponseModels()).hasSize(2).containsExactlyInAnyOrderElementsOf(rawResponseModels);
        Assertions.assertThat(result.lunaticRawDataModels()).hasSize(1).containsExactlyInAnyOrderElementsOf(lunaticRawData);
    }

    @Test
    void getCombinedRawData_shouldHandleEmptyLists() {
        //GIVEN
        String interrogationId = "INTERROGATION_EMPTY";
        Mockito.when(rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId))
                .thenReturn(List.of());
        Mockito.when(lunaticJsonRawDataPersistencePort.findRawDataByInterrogationId(interrogationId))
                .thenReturn(List.of());

        //WHEN
        CombinedRawDataDto result = combinedRawDataService.getCombinedRawDataByInterrogationId(interrogationId);

        //THEN
        Assertions.assertThat(result.rawResponseModels()).isEmpty();
        Assertions.assertThat(result.lunaticRawDataModels()).isEmpty();
    }

}
