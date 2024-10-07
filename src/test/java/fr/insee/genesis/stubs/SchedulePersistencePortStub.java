package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class SchedulePersistencePortStub implements SchedulePersistencePort {
    List<ScheduleDocument> mongoStub = new ArrayList<>();

    @Override
    public List<ScheduleModel> getAll() {
        return ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub);
    }

    @Override
    public void saveAll(List<ScheduleModel> scheduleDocuments) {
        mongoStub.addAll(ScheduleDocumentMapper.INSTANCE.listModelToListDocument(scheduleDocuments));
    }

    @Override
    public List<ScheduleModel> findBySurveyName(String surveyName) {
        return ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub.stream().filter(surveySchedule -> surveySchedule.getSurveyName().equals(surveyName)).toList());
    }

    @Override
    public void deleteBySurveyName(String surveyName) {
        mongoStub.removeIf(surveySchedule -> surveySchedule.getSurveyName().equals(surveyName));
    }

    @Override
    public long countSchedules() {
        return mongoStub.size();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(ScheduleModel scheduleModel) {
        List<KraftwerkExecutionSchedule> kraftwerkExecutionSchedulesToRemove = new ArrayList<>(scheduleModel.getKraftwerkExecutionScheduleList().stream().filter(
                kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
        ).toList());
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove : kraftwerkExecutionSchedulesToRemove){
            scheduleModel.getKraftwerkExecutionScheduleList().remove(kraftwerkExecutionScheduleToRemove);
            log.info("Removed kraftwerk execution schedule on {} because it is expired since {}", scheduleModel.getSurveyName(),
                    kraftwerkExecutionScheduleToRemove.getScheduleEndDate());
        }
        //Update mongo stub
        mongoStub.removeIf(scheduleDocument -> scheduleDocument.getSurveyName().equals(scheduleModel.getSurveyName()));
        mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(scheduleModel));
        return kraftwerkExecutionSchedulesToRemove;
    }
}