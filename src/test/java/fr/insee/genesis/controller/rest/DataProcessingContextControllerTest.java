package fr.insee.genesis.controller.rest;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DataProcessingContextControllerTest {

    DataProcessingContextApiPort dataProcessingContextApiPort;

    DataProcessingContextController dataProcessingContextController;

    @BeforeEach
    void setUp() {
        dataProcessingContextApiPort = mock(DataProcessingContextApiPort.class);
        dataProcessingContextController = new DataProcessingContextController(
                dataProcessingContextApiPort,
                new FileUtils(TestConstants.getConfigStub())
        );
    }

    @Test
    @SneakyThrows
    void saveContextWithCollectionInstrumentId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        boolean withReview = true;

        //WHEN
        dataProcessingContextController.saveContextWithCollectionInstrumentId(
                collectionInstrumentId, withReview
        );

        //THEN
        verify(dataProcessingContextApiPort, times(1))
                .saveContextByCollectionInstrumentId(collectionInstrumentId, withReview);
    }

    @Test
    @SneakyThrows
    void saveContextWithCollectionInstrumentId_null_review_test() {
        //GIVEN
        String collectionInstrumentId = "test";

        //WHEN
        dataProcessingContextController.saveContextWithCollectionInstrumentId(
                collectionInstrumentId, null
        );

        //THEN
        verify(dataProcessingContextApiPort, times(1))
                .saveContextByCollectionInstrumentId(collectionInstrumentId, false);
    }

    @Test
    @SneakyThrows
    void getReviewIndicatorByCollectionInstrumentId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        boolean withReview = true;
        doReturn(withReview).when(dataProcessingContextApiPort).getReviewByCollectionInstrumentId(any());

        //WHEN
        ResponseEntity<Object> response = dataProcessingContextController.getReviewIndicatorByCollectionInstrumentId(
                collectionInstrumentId
        );

        //THEN
        Assertions.assertThat(response.getBody())
                .isNotNull()
                .isInstanceOf(Boolean.class)
                .isEqualTo(withReview);
    }

    @Test
    void saveScheduleWithCollectionInstrumentId_test() {
        //GIVEN
        //WHEN
        //THEN
    }

    @Test
    void getAllSchedulesV2() {
        //GIVEN
        //WHEN
        //THEN
    }

    @Test
    void setSurveyLastExecutionByCollectionInstrumentId() {
        //GIVEN
        //WHEN
        //THEN
    }

    @Test
    void deleteSchedulesByCollectionInstrumentId() {
        //GIVEN
        //WHEN
        //THEN
    }

    @Test
    void deleteExpiredSchedules() {
        //GIVEN
        //WHEN
        //THEN
    }
}