package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SchedulePersistencePortStub implements SchedulePersistencePort {
    List<StoredSurveySchedule> mongoStub = new ArrayList<>();

    @Override
    public List<StoredSurveySchedule> getAll() {
        return mongoStub;
    }

    @Override
    public void saveAll(List<StoredSurveySchedule> storedSurveySchedules) {
        mongoStub.addAll(storedSurveySchedules);
    }

    @Override
    public List<StoredSurveySchedule> findBySurveyName(String surveyName) {
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
