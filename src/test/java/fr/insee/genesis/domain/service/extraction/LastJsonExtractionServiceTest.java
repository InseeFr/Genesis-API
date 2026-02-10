package fr.insee.genesis.domain.service.extraction;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LastJsonExtractionServiceTest {
    LastJsonExtractionPersistencePort lastJsonExtractionPersistencePort;
    LastJsonExtractionService lastJsonExtractionService;

    @Captor
    ArgumentCaptor<LastJsonExtractionModel> lastJsonExtractionModelArgumentCaptor;

    @BeforeEach
    void setUp() {
        lastJsonExtractionPersistencePort = mock(LastJsonExtractionPersistencePort.class);
        lastJsonExtractionService = new LastJsonExtractionService(
                lastJsonExtractionPersistencePort
        );
        lastJsonExtractionModelArgumentCaptor = ArgumentCaptor.forClass(LastJsonExtractionModel.class);
    }

    @Test
    void recordDate_test() {
        //GIVEN
        LastJsonExtractionModel lastJsonExtractionModel = LastJsonExtractionModel.builder().build();
        lastJsonExtractionModel.setCollectionInstrumentId("test");
        lastJsonExtractionModel.setMode(Mode.WEB);

        //WHEN
        lastJsonExtractionService.recordDate(lastJsonExtractionModel);

        //THEY
        verify(lastJsonExtractionPersistencePort, times(1))
                .save(lastJsonExtractionModelArgumentCaptor.capture());
        Assertions.assertThat(lastJsonExtractionModelArgumentCaptor.getValue()).isNotNull();
        Assertions.assertThat(lastJsonExtractionModelArgumentCaptor.getValue().getId())
                .isNotNull().isEqualTo(String.format("%s_%s"
                        ,lastJsonExtractionModel.getCollectionInstrumentId()
                        ,lastJsonExtractionModel.getMode()));
    }

    @Test
    @SneakyThrows
    void getLastExtractionDate_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;
        LastJsonExtractionModel expected = LastJsonExtractionModel.builder().build();
        doReturn(expected).when(lastJsonExtractionPersistencePort).getLastExecutionDate(anyString(), any());

        //WHEN + THEN
        Assertions.assertThat(lastJsonExtractionService.getLastExtractionDate(
                collectionInstrumentId, mode
                )).isEqualTo(expected);
        verify(lastJsonExtractionPersistencePort, times(1))
                .getLastExecutionDate(collectionInstrumentId, mode);
    }

    @Test
    @SneakyThrows
    void delete_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;

        //WHEN
        lastJsonExtractionService.delete(collectionInstrumentId, mode);

        //THEN
        verify(lastJsonExtractionPersistencePort, times(1))
                .getLastExecutionDate(collectionInstrumentId, mode);
        verify(lastJsonExtractionPersistencePort, times(1))
                .delete(collectionInstrumentId, mode);
        }

    @Test
    @SneakyThrows
    void delete_null_mode_test() {
        //GIVEN
        String collectionInstrumentId = "test";

        //WHEN
        lastJsonExtractionService.delete(collectionInstrumentId, null);

        //THEN
        verify(lastJsonExtractionPersistencePort, never())
                .getLastExecutionDate(collectionInstrumentId, null);
        verify(lastJsonExtractionPersistencePort, times(1))
                .delete(collectionInstrumentId, null);
    }
}