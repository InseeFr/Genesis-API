package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;

import java.util.List;

public interface SchedulePersistencePort {
    List<StoredSurveySchedule> getAll();

    void saveAll(List<StoredSurveySchedule> storedSurveySchedules);

    List<StoredSurveySchedule> findBySurveyName(String surveyName);

    void deleteBySurveyName(String surveyName);

    long countSchedules();
}
