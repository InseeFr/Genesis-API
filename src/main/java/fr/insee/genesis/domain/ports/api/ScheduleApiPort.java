package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleApiPort {
    List<ScheduleDocument> getAllSchedules();

    void addSchedule(String surveyName,
                     ServiceToCall serviceToCall,
                     String frequency,
                     LocalDateTime scheduleBeginDateString,
                     LocalDateTime scheduleEndDateString) throws InvalidCronExpressionException;

    void updateLastExecutionName(String surveyName) throws NotFoundException;
}
