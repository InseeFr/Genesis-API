package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.LastExtractionResponseDto;
import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LastJsonExtractionApiPort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JsonExtractionControllerTest {
    LastJsonExtractionApiPort lastJsonExtractionApiPort;

    JsonExtractionController jsonExtractionController;

    @Captor
    ArgumentCaptor<LastJsonExtractionModel> lastJsonExtractionModelArgumentCaptor;

    @BeforeEach
    void setUp() {
        lastJsonExtractionApiPort = mock(LastJsonExtractionApiPort.class);
        jsonExtractionController = new JsonExtractionController(
                lastJsonExtractionApiPort
        );
        lastJsonExtractionModelArgumentCaptor = ArgumentCaptor.forClass(LastJsonExtractionModel.class);
    }

    @Test
    void saveLastJsonExtractionDate_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;

        //WHEN
        jsonExtractionController.saveLastJsonExtractionDate(collectionInstrumentId, mode);

        //THEN
        verify(lastJsonExtractionApiPort, times(1))
                .recordDate(lastJsonExtractionModelArgumentCaptor.capture());
        LastJsonExtractionModel lastJsonExtractionModel = lastJsonExtractionModelArgumentCaptor.getValue();
        Assertions.assertThat(lastJsonExtractionModel.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(lastJsonExtractionModel.getMode()).isEqualTo(mode);
        Assertions.assertThat(lastJsonExtractionModel.getLastExtractionDate()).isNotNull();
    }

    @Test
    @SneakyThrows
    void getLastJsonExtractionDate_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;
        LocalDateTime lastExtractionDate = LocalDateTime.now();

        LastJsonExtractionModel lastJsonExtractionModel = LastJsonExtractionModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .mode(mode)
                .lastExtractionDate(lastExtractionDate)
                .build();
        doReturn(lastJsonExtractionModel).when(lastJsonExtractionApiPort).getLastExtractionDate(
                any(), any()
        );

        //WHEN
        ResponseEntity<LastExtractionResponseDto> response =
                jsonExtractionController.getLastJsonExtractionDate(collectionInstrumentId, mode);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getLastExtractionDate()).isEqualTo(lastExtractionDate.toString());
    }

    @Test
    @SneakyThrows
    void deleteJsonExtractionDate() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;

        //WHEN
        ResponseEntity<Object> response = jsonExtractionController.deleteJsonExtractionDate(collectionInstrumentId, mode);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(lastJsonExtractionApiPort).delete(collectionInstrumentId, mode);
    }
}