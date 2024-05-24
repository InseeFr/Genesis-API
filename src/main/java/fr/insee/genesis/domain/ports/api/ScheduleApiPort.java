package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleApiPort {
    List<StoredSurveySchedule> getAllSchedules();

    void addSchedule(String surveyName,
                     ServiceToCall serviceToCall,
                     String frequency,
                     LocalDateTime scheduleBeginDateString,
                     LocalDateTime scheduleEndDateString) throws InvalidCronExpressionException;

    void deleteSchedule(String surveyName) throws NotFoundException;

    void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException;

    long countSchedules();
}
