package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
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
