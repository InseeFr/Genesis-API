package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.stream.Stream;

class DataProcessingContextControllerTest {
    //Given
    private static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    private static DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;
    private static DataProcessingContextController dataProcessingContextController;

    @BeforeEach
    void clean() throws IOException {
        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        dataProcessingContextController = new DataProcessingContextController(
                new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub),
                new FileUtils(new ConfigStub())
        );
        //Clean genesis_deleted_schedules log folder
        if(Files.exists(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME))) {
            try(Stream<Path> files = Files.list(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                    .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME))){
                for (Path filePath : files.toList()) {
                    Files.deleteIfExists(filePath);
                }
            }
        }
    }

    @Test
    @Disabled
    @SneakyThrows
    void deleteScheduleTest_collectionInstrumentId(){
        //When
        dataProcessingContextController.deleteSchedulesByCollectionInstrumentId("TESTSURVEY_CI");

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY_CI")
        ).isEmpty();
    }

    @Test
    void deleteExpiredScheduleTest_execution() throws GenesisException {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                null,
                "TESTSURVEYADDED",
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
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2000").doesNotContain("5023");
    }

    @Test
    void deleteExpiredScheduleTest_execution_collectionInstrumentId() throws GenesisException {
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
    void deleteExpiredScheduleTest_wholeSurvey() throws GenesisException {
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
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED")
        ).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isEmpty();


        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2001","2002");
    }

    @Test
    void deleteExpiredScheduleTest_wholeSurvey_collectionInstrumentId() throws GenesisException {
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
    void deleteExpiredScheduleTest_appendLog() throws GenesisException {
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
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEYADDED2")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument
                        .getCollectionInstrumentId().equals("TESTSURVEYADDED2")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedules to only one log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED2.json")
                .toFile()).exists().content().isNotEmpty().contains("2000","2001");
    }

    @Test
    void deleteExpiredScheduleTest_appendLog_collectionInstrumentId() throws GenesisException {
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
    @SneakyThrows
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
    @Disabled
    @SneakyThrows
    void getReview_no_context_test_collectionInstrumentId(){
        //WHEN
        ResponseEntity<Object> response = dataProcessingContextController.getReviewIndicatorByCollectionInstrumentId(
                "TESTPARTITIONIDNOCONTEXT_CI"
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
