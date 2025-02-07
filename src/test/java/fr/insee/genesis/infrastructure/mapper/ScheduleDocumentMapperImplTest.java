package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapperImpl;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ScheduleDocumentMapperImplTest {

    //Given
    static ScheduleDocumentMapper scheduleDocumentMapperImplStatic;
    static ScheduleDocument scheduleDocument;

    static ScheduleModel scheduleModel;

    @BeforeAll
    static void init(){
        scheduleDocumentMapperImplStatic = new ScheduleDocumentMapperImpl();

        scheduleDocument = new ScheduleDocument();
        scheduleDocument.setSurveyName("TESTCAMPAIGNID");
        scheduleDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());

        scheduleModel = ScheduleModel.builder()
                .surveyName("TESTCAMPAIGNID")
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();

        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        );

        scheduleDocument.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
    }

    //When + Then
    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(scheduleDocumentMapperImplStatic.documentToModel(null)).isNull();
        Assertions.assertThat(scheduleDocumentMapperImplStatic.modelToDocument(null)).isNull();
        Assertions.assertThat(scheduleDocumentMapperImplStatic.listDocumentToListModel(null)).isNull();
        Assertions.assertThat(scheduleDocumentMapperImplStatic.listModelToListDocument(null)).isNull();
    }

    @Test
    @DisplayName("Should convert document to DTO")
    void shouldReturnDocumentDtoFromDocument(){
        ScheduleModel scheduleModel1 = scheduleDocumentMapperImplStatic.documentToModel(scheduleDocument);

        Assertions.assertThat(scheduleModel1.getSurveyName()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(scheduleModel1.getLastExecution()).isNull();
        Assertions.assertThat(scheduleModel1.getKraftwerkExecutionScheduleList()).hasSize(1);

        Assertions.assertThat(scheduleModel1.getKraftwerkExecutionScheduleList()).filteredOn(kraftwerkExecutionSchedule ->
            kraftwerkExecutionSchedule.getFrequency().equals("0 0 6 * * *")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        ScheduleDocument scheduleDocument1 = scheduleDocumentMapperImplStatic.modelToDocument(scheduleModel);

        Assertions.assertThat(scheduleDocument1.getSurveyName()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(scheduleDocument1.getLastExecution()).isNull();
        Assertions.assertThat(scheduleDocument1.getKraftwerkExecutionScheduleList()).hasSize(1);

        Assertions.assertThat(scheduleDocument1.getKraftwerkExecutionScheduleList()).filteredOn(kraftwerkExecutionSchedule ->
                kraftwerkExecutionSchedule.getFrequency().equals("0 0 6 * * *")
        ).isNotEmpty();
    }


    @Test
    @DisplayName("Should convert document list to DTO list")
    void shouldReturnDocumentLDtoListFromDocumentList(){
        List<ScheduleDocument> scheduleDocumentList = new ArrayList<>();
        scheduleDocumentList.add(scheduleDocument);

        List<ScheduleModel> scheduleModelList = scheduleDocumentMapperImplStatic.listDocumentToListModel(scheduleDocumentList);

        Assertions.assertThat(scheduleModelList.getFirst().getSurveyName()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(scheduleModelList.getFirst().getLastExecution()).isNull();
        Assertions.assertThat(scheduleModelList.getFirst().getKraftwerkExecutionScheduleList()).hasSize(1);

        Assertions.assertThat(scheduleModelList.getFirst().getKraftwerkExecutionScheduleList()).filteredOn(kraftwerkExecutionSchedule ->
                kraftwerkExecutionSchedule.getFrequency().equals("0 0 6 * * *")
        ).isNotEmpty();

        Assertions.assertThat(scheduleModelList.getFirst().getKraftwerkExecutionScheduleList()).filteredOn(kraftwerkExecutionSchedule ->
                kraftwerkExecutionSchedule.getFrequency().equals("0 0 6 * * *")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO list to document list")
    void shouldReturnDocumentListFromDocumentDtoList(){
        List<ScheduleModel> scheduleModelList = new ArrayList<>();
        scheduleModelList.add(scheduleModel);

        List<ScheduleDocument> scheduleDocumentList = scheduleDocumentMapperImplStatic.listModelToListDocument(scheduleModelList);

        Assertions.assertThat(scheduleDocumentList.getFirst().getSurveyName()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(scheduleDocumentList.getFirst().getLastExecution()).isNull();
        Assertions.assertThat(scheduleDocumentList.getFirst().getKraftwerkExecutionScheduleList()).hasSize(1);

        Assertions.assertThat(scheduleDocumentList.getFirst().getKraftwerkExecutionScheduleList()).filteredOn(schedule ->
                schedule.getFrequency().equals("0 0 6 * * *")
        ).isNotEmpty();
    }
}
