package integration_tests;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import integration_tests.stubs.ConfigStub;
import integration_tests.stubs.DataProcessingContextPersistancePortStub;
import integration_tests.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DataProcessingContextServiceTest {
    //Given
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static DataProcessingContextService dataProcessingContextService;
    static DataProcessingContextPersistancePortStub dataProcessingContextPersistencePortStub;
    FileUtils fileUtils = new FileUtils(new ConfigStub());

    @BeforeAll
    static void init(){
        dataProcessingContextPersistencePortStub = new DataProcessingContextPersistancePortStub();
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        dataProcessingContextService = new DataProcessingContextService(dataProcessingContextPersistencePortStub, surveyUnitPersistencePortStub);
    }

    @BeforeEach
    void reset(){
        dataProcessingContextPersistencePortStub.getMongoStub().clear();
        surveyUnitPersistencePortStub.getMongoStub().clear();

        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        ));
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setPartitionId("TEST");
        doc.setCollectionInstrumentId("TEST");
        doc.setKraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList);
        doc.setWithReview(false);
        dataProcessingContextPersistencePortStub.getMongoStub().add(doc);
    }

    @Test
    void getAllSchedules_test(){
        //When + Then
        Assertions.assertThat(dataProcessingContextService.getAllSchedules()).hasSize(1);
    }

    @Test
    void saveKraftwerkExecutionSchedule_test_new_survey() throws GenesisException {
        //When
        dataProcessingContextService.saveKraftwerkExecutionSchedule("TEST2",
                ServiceToCall.GENESIS,
                "0 0 6 * * *",
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        );

        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getPartitionId()).isEqualTo("TEST2");
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo("0 0 6 * * *");
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getScheduleBeginDate()).isEqualTo(LocalDateTime.MIN);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getScheduleEndDate()).isEqualTo(LocalDateTime.MAX);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.GENESIS);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void saveKraftwerkExecutionSchedule_test_old_survey() throws GenesisException {
        //When
        dataProcessingContextService.saveKraftwerkExecutionSchedule("TEST",
                ServiceToCall.GENESIS,
                "0 0 0 6 * *",
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        );

        //Then
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getPartitionId()).isEqualTo("TEST");
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList()).hasSize(2);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getFrequency()).isEqualTo("0 0 0 6 * *");
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getScheduleBeginDate()).isEqualTo(LocalDateTime.MIN);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getScheduleEndDate()).isEqualTo(LocalDateTime.MAX);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getServiceToCall()).isEqualTo(ServiceToCall.GENESIS);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getTrustParameters()).isNull();
    }

    @Test
    void deleteSchedule_test() throws GenesisException {
        //When
        dataProcessingContextService.deleteSchedules("TEST");

        //Then
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TEST")
        ).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TEST")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isEmpty();
    }

    @Test
    void updateLastExecutionName_test() throws GenesisException {
        //Given
        LocalDateTime localDateTime = LocalDateTime.now();

        //When
        dataProcessingContextService.updateLastExecutionDateByCollectionInstrumentId("TEST", localDateTime);

        //Then
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getLastExecution()).isNotNull().isEqualTo(localDateTime);
    }

    @Test
    void removeExpiredSchedules_test_existing_schedule() throws GenesisException {
        //Given
        //Expired schedule
        dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.of(2000,1,1,1,1,1),
                null
        ));

        //When
        dataProcessingContextService.deleteExpiredSchedules(fileUtils.getLogFolder());

        //Then
        //Execution schedule deleted
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().getFirst().getScheduleEndDate())
                .isEqualTo(LocalDateTime.MAX);
    }
    @Test
    void removeExpiredSchedules_test_delete_document() throws GenesisException {
        //Given
        //Expired schedule + new survey
        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.of(2000,1,1,1,1,1),
                null
        ));
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setCollectionInstrumentId("TEST2");
        doc.setKraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList);
        doc.setWithReview(false);
        dataProcessingContextPersistencePortStub.getMongoStub().add(doc);

        //When
        dataProcessingContextService.deleteExpiredSchedules(fileUtils.getLogFolder());

        //Then
        //Survey schedule document deleted
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().stream().filter(
                scheduleDocument -> scheduleDocument.getCollectionInstrumentId().equals("TEST2")
        ).toList()).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistencePortStub.getMongoStub().stream().filter(
                scheduleDocument -> scheduleDocument.getCollectionInstrumentId().equals("TEST2")
        ).toList().getFirst().getKraftwerkExecutionScheduleList()).isEmpty();

    }

    @Test
    void getContext_shouldThrow500IfMultipleCollectionInstruments() {
        // Given
        SurveyUnitModel su1 = SurveyUnitModel.builder()
                .collectionInstrumentId("CAMPAIGN1")
                .interrogationId("00001")
                .build();
        SurveyUnitModel su2 = SurveyUnitModel.builder()
                .collectionInstrumentId("CAMPAIGN2")
                .interrogationId("00001")
                .build();
        List<SurveyUnitModel> sus = new ArrayList<>();
        sus.add(su1);
        sus.add(su2);
        surveyUnitPersistencePortStub.saveAll(sus);

        // When & Then
        GenesisException ex = assertThrows(GenesisException.class, () -> dataProcessingContextService.getContext("00001"));
        //To ensure test is portable on Unix/Linux/macOS and windows systems
        String normalizedMessage = ex.getMessage().replaceAll("\\r?\\n", "");
        Assertions.assertThat(ex.getStatus()).isEqualTo(500);
        Assertions.assertThat(normalizedMessage).isEqualTo("Multiple collection instruments for interrogation 00001");
    }

    @Test
    void getContext_shouldThrow404IfNoInterrogations() {
        // When & Then
        GenesisException ex = assertThrows(GenesisException.class, () -> dataProcessingContextService.getContext("00001"));
        Assertions.assertThat(ex.getStatus()).isEqualTo(404);
        Assertions.assertThat(ex.getMessage()).isEqualTo("No interrogation in database with id 00001");
    }

    @Test
    void getContext_shouldReturnContextIfOneCollectionInstrument() throws GenesisException {
        // Given
        SurveyUnitModel su1 = SurveyUnitModel.builder()
                .collectionInstrumentId("TEST")
                .interrogationId("00001")
                .build();
        List<SurveyUnitModel> sus = new ArrayList<>();
        sus.add(su1);
        surveyUnitPersistencePortStub.saveAll(sus);
        DataProcessingContextModel result = dataProcessingContextService.getContext("00001");

        // When & Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCollectionInstrumentId()).isEqualTo("TEST");
        Assertions.assertThat(result.isWithReview()).isFalse();
    }



}
