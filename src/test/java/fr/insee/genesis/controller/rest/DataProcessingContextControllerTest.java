package fr.insee.genesis.controller.rest;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @SneakyThrows
    void saveScheduleWithCollectionInstrumentId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        ServiceToCall serviceToCall = ServiceToCall.GENESIS;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMinutes(1);
        boolean useEncryption = false;


        //WHEN
        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(
                collectionInstrumentId,
                serviceToCall,
                frequency,
                scheduleBeginDate,
                scheduleEndDate,
                useEncryption,
                "",
                "",
                false
        );

        //THEN
        verify(dataProcessingContextApiPort, times(1))
                .saveKraftwerkExecutionScheduleByCollectionInstrumentId(
                        collectionInstrumentId,
                        serviceToCall,
                        frequency,
                        scheduleBeginDate,
                        scheduleEndDate,
                        null
                );
    }

    @Test
    @SneakyThrows
    void saveScheduleWithCollectionInstrumentId_with_encryption_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        ServiceToCall serviceToCall = ServiceToCall.GENESIS;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMinutes(1);
        boolean useEncryption = true;
        String encryptionVaultPath = "test1";
        String encryptionOutputFolder = "test2";


        //WHEN
        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(
                collectionInstrumentId,
                serviceToCall,
                frequency,
                scheduleBeginDate,
                scheduleEndDate,
                useEncryption,
                encryptionVaultPath,
                encryptionOutputFolder,
                false
        );

        //THEN
        verify(dataProcessingContextApiPort, times(1))
                .saveKraftwerkExecutionScheduleByCollectionInstrumentId(
                        eq(collectionInstrumentId),
                        eq(serviceToCall),
                        eq(frequency),
                        eq(scheduleBeginDate),
                        eq(scheduleEndDate),
                        any()
                );
    }


    @Test
    void saveScheduleWithCollectionInstrumentId_wrong_cron_test() {
        //GIVEN
        String frequency = "dsadasd 0 6 * * *";

        //WHEN
        ResponseEntity<Object> response = dataProcessingContextController.saveScheduleWithCollectionInstrumentId(
                null,
                null,
                frequency,
                null,
                null,
                false,
                null,
                null,
                false
        );

        //THEN
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //TODO remove schedule endpoints and tests when Bangles V1 is deployed
}