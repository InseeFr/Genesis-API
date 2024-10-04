package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;

import java.util.List;

public interface SchedulePersistencePort {
    List<ScheduleModel> getAll();

    void saveAll(List<ScheduleModel> scheduleDocuments);

    List<ScheduleModel> findBySurveyName(String surveyName);

    void deleteBySurveyName(String surveyName);

    long countSchedules();

    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(ScheduleModel scheduleModel);
}
