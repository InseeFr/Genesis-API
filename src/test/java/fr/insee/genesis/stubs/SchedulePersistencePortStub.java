package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SchedulePersistencePortStub implements SchedulePersistencePort {
    List<SurveyScheduleDocument> mongoStub = new ArrayList<>();

    @Override
    public List<SurveyScheduleDocument> getAll() {
        return mongoStub;
    }

    @Override
    public void saveAll(List<SurveyScheduleDocument> surveyScheduleDocuments) {
        mongoStub.addAll(surveyScheduleDocuments);
    }

    @Override
    public List<SurveyScheduleDocument> findBySurveyName(String surveyName) {
        return mongoStub.stream().filter(surveySchedule -> surveySchedule.getSurveyName().equals(surveyName)).toList();
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
