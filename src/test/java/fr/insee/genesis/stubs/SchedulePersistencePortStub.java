package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
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
}
