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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
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
    void getAllSchedulesV2Test() {
        //When
        ResponseEntity<Object> response = dataProcessingContextController.getAllSchedulesV2();

        //Then
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void addScheduleWithoutEncryptionTest() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithoutEncryptionTestUsingCollectionInstrumentId() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY_CI";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithoutEncryptionTest_nullServiceToCall() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = null;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.MAIN);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithoutEncryptionTest_nullServiceToCall_collectionInstrumentId() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY_CI";
        ServiceToCall serviceToCall = null;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.MAIN);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithEncryptionTest() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, true,
                "testvault/testkey",
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("OUT_ENCRYPTED").resolve(collectionInstrumentId).toString(),
                false
        );

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getInputPath()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getOutputFolder()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getVaultPath()).isEqualTo(
                "testvault/testkey");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().isUseSignature()).isFalse();
    }

    @Test
    void addScheduleWithEncryptionTest_collectionInstrumentId() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY_CI";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, true,
                "testvault/testkey",
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("OUT_ENCRYPTED").resolve(collectionInstrumentId).toString(),
                false
        );

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getInputPath()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getOutputFolder()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getVaultPath()).isEqualTo(
                "testvault/testkey");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().isUseSignature()).isFalse();
    }

    @Test
    void addAdditionnalScheduleTest() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate,
                scheduleEndDate,
                false, "", "", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addAdditionnalScheduleTest_collectionInstrumentId() throws GenesisException {
        //When
        String collectionInstrumentId = "TESTADDSURVEY_CI"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleDedupTest() throws GenesisException {
        //Given
        addNewDocumentToStub();

        //When
        String collectionInstrumentId = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleDedupTest_collectionInstrumentId() throws GenesisException {
        //Given
        addNewDocumentToStubWithCollectionInstrumentId();

        //When
        String collectionInstrumentId = "TESTSURVEY_CI"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(collectionInstrumentId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals(collectionInstrumentId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void updateLastExecutionTest() throws GenesisException {
        //Given
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY", LocalDateTime.now());

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void updateLastExecutionTest_collectionInstrumentId() throws GenesisException {
        //Given
        addNewDocumentToStubWithCollectionInstrumentId();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY_CI", LocalDateTime.now());

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY_CI")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void setLastExecutionTestToNull() throws GenesisException {
        //Given
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY", null);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNull();
    }

    @Test
    void setLastExecutionTestToNull_collectionInstrumentId() throws GenesisException {
        //Given
        addNewDocumentToStubWithCollectionInstrumentId();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY_CI", null);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY_CI")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNull();
    }

    @Test
    void setLastExecutionTest() throws GenesisException {
        //Given
        LocalDateTime date = LocalDateTime.now();
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY", date);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isEqualTo(date);
    }

    @Test
    void setLastExecutionTest_collectionInstrumentId() throws GenesisException {
        //Given
        LocalDateTime date = LocalDateTime.now();
        addNewDocumentToStubWithCollectionInstrumentId();

        //When
        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("TESTSURVEY_CI", date);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getCollectionInstrumentId().equals("TESTSURVEY_CI")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isEqualTo(date);
    }

    @Test
    void wrongFrequencyTest() {
        String collectionInstrumentId = "TESTSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "ERROR";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(
                                collectionInstrumentId,
                                serviceToCall,
                                frequency,
                                scheduleBeginDate,
                                scheduleEndDate,
                                false,
                                "",
                                "",
                                false
                        )
                )
                .isInstanceOfSatisfying(GenesisException.class, exception -> {
                    Assertions.assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Assertions.assertThat(exception.getMessage()).isEqualTo("Wrong frequency syntax");
                });
    }

    @Test
    void wrongFrequencyTest_collectionInstrumentId() {
        String collectionInstrumentId = "TESTSURVEY_CI";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "ERROR";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.saveScheduleWithCollectionInstrumentId(
                                collectionInstrumentId,
                                serviceToCall,
                                frequency,
                                scheduleBeginDate,
                                scheduleEndDate,
                                false,
                                "",
                                "",
                                false
                        )
                )
                .isInstanceOfSatisfying(GenesisException.class, exception -> {
                    Assertions.assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Assertions.assertThat(exception.getMessage()).isEqualTo("Wrong frequency syntax");
                });
    }

    @Test
    void notFoundTest() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.setSurveyLastExecution("ERROR", LocalDateTime.now())
                )
                .isInstanceOf(GenesisException.class);
    }

    @Test
    void notFoundTest_collectionInstrumentId() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.setSurveyLastExecutionByCollectionInstrumentId("ERROR", LocalDateTime.now())
                )
                .isInstanceOfSatisfying(GenesisException.class, exception -> {
                    Assertions.assertThat(exception.getStatus().is4xxClientError()).isTrue();
                });
    }

    @Test
    void deleteScheduleTest() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.deleteSchedules("TESTSURVEY")
                )
                .isInstanceOf(GenesisException.class)
                .hasMessage("Context not found");
    }

    @Test
    void deleteScheduleTest_collectionInstrumentId() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.deleteSchedulesByCollectionInstrumentId("TESTSURVEY_CI")
                )
                .isInstanceOf(GenesisException.class)
                .hasMessage("Context not found");
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
    void getReview_test(boolean withReview) throws GenesisException {
        //GIVEN
        String collectionInstrumentId = "TESTPARTITION";
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setCollectionInstrumentId("TESTPARTITION");
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getReview_test_collectionInstrumentId(boolean withReview) throws GenesisException {
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
    void getReview_no_context_test() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.getReviewIndicatorByCollectionInstrumentId("TESTcollectionInstrumentIdNOCONTEXT")
                )
                .isInstanceOf(GenesisException.class)
                .hasMessage("Data processing context not found");
    }

    @Test
    void getReview_no_context_test_collectionInstrumentId() {
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextController.getReviewIndicatorByCollectionInstrumentId(
                                "TESTcollectionInstrumentIdNOCONTEXT_CI"
                        )
                )
                .isInstanceOf(GenesisException.class)
                .hasMessage("Data processing context not found");
    }


    //UTILITY
    private void addNewDocumentToStub() {
        DataProcessingContextDocument dataProcessingContextDocumentTest = new DataProcessingContextDocument();
        dataProcessingContextDocumentTest.setCollectionInstrumentId("TESTSURVEY");
        dataProcessingContextDocumentTest.setKraftwerkExecutionScheduleList(new ArrayList<>());
        dataProcessingContextDocumentTest.setWithReview(false);

        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(dataProcessingContextDocumentTest);
    }

    private void addNewDocumentToStubWithCollectionInstrumentId() {
        DataProcessingContextDocument dataProcessingContextDocumentTest = new DataProcessingContextDocument();
        dataProcessingContextDocumentTest.setCollectionInstrumentId("TESTSURVEY_CI");
        dataProcessingContextDocumentTest.setKraftwerkExecutionScheduleList(new ArrayList<>());
        dataProcessingContextDocumentTest.setWithReview(false);
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(dataProcessingContextDocumentTest);
    }
}
