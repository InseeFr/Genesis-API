package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;

import java.util.List;

public interface SchedulePersistencePort {
    List<SurveyScheduleDocument> getAll();

    void saveAll(List<SurveyScheduleDocument> surveyScheduleDocuments);

    List<SurveyScheduleDocument> findBySurveyName(String surveyName);

    void deleteBySurveyName(String surveyName);

    long countSchedules();
}
