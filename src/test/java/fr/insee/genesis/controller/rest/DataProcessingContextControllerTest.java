package fr.insee.genesis.controller.rest;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataProcessingContextControllerTest {

    @Mock
    DataProcessingContextApiPort dataProcessingContextApiPort;

    DataProcessingContextController dataProcessingContextController;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void deleteScheduleTest_collectionInstrumentId(){
        //When
        dataProcessingContextController.deleteSchedulesByCollectionInstrumentId("TESTSURVEY_CI");

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEY_CI")
        ).isEmpty();
    }

    @Test
    void deleteExpiredScheduleTest_execution() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED",
                null,
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2000").doesNotContain("5023");
    }

    @Test
    void deleteExpiredScheduleTest_execution_collectionInstrumentId() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                null,
                "TESTSURVEYADDED_CI",
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED_CI")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED_CI")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED_CI.json")
                .toFile()).exists().content().isNotEmpty().contains("2000").doesNotContain("5023");
    }

    @Test
    void deleteExpiredScheduleTest_wholeSurvey() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED",
                "TESTSURVEYADDED",
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2002, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2002, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")
        ).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isEmpty();


        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2001","2002");
    }

    @Test
    void deleteExpiredScheduleTest_wholeSurvey_collectionInstrumentId() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                null,
                "TESTSURVEYADDED_CI",
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2002, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2002, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED_CI")
        ).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED_CI")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isEmpty();


        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED_CI.json")
                .toFile()).exists().content().isNotEmpty().contains("2001","2002");
    }

    @Test
    void deleteExpiredScheduleTest_appendLog() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED2",
                "TESTSURVEYADDED2",
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedules deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED2")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument
                        .getPartitionId().equals("TESTSURVEYADDED2")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedules to only one log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED2.json")
                .toFile()).exists().content().isNotEmpty().contains("2000","2001");
    }

    @Test
    void deleteExpiredScheduleTest_appendLog_collectionInstrumentId() {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                null,
                "TESTSURVEYADDED2_CI",
                null,
                new ArrayList<>(),
                null,
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedules deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED2_CI")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument
                        .getCollectionInstrumentId().equals("TESTSURVEYADDED2_CI")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedules to only one log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED2_CI.json")
                .toFile()).exists().content().isNotEmpty().contains("2000","2001");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getReview_test_collectionInstrumentId(boolean withReview){
        //GIVEN
        String collectionInstrumentId = "TESTPARTITION_CI";
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setCollectionInstrumentId("TESTPARTITION_CI");
        doc.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc.setWithReview(withReview);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc);

        //WHEN
        ResponseEntity<Object> response = dataProcessingContextController.getReviewIndicatorByCollectionInstrumentId(collectionInstrumentId);

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getBody().getClass()).isEqualTo(Boolean.class);
        Assertions.assertThat((Boolean) response.getBody()).isEqualTo(withReview);
    }

    @Test
    void getReview_no_context_test_collectionInstrumentId(){
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
}
