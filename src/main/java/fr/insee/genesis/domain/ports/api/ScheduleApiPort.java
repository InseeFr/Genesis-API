package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.TrustParameters;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleApiPort {
    List<SurveyScheduleDocument> getAllSchedules();

    void addSchedule(String surveyName,
                     ServiceToCall serviceToCall,
                     String frequency,
                     LocalDateTime scheduleBeginDateString,
                     LocalDateTime scheduleEndDateString,
                     TrustParameters trustParameters
                     ) throws InvalidCronExpressionException;

    void deleteSchedule(String surveyName) throws NotFoundException;

    void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException;

    long countSchedules();
}
