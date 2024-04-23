package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleApiPort {
    List<StoredSurveySchedule> getAllSchedules();

    void addSchedule(String surveyName,
                     ServiceToCall serviceToCall,
                     String frequency,
                     LocalDateTime scheduleBeginDateString,
                     LocalDateTime scheduleEndDateString) throws InvalidCronExpressionException;

    void addSchedule(String surveyName,
                     ServiceToCall serviceToCall,
                     String frequency,
                     LocalDateTime scheduleBeginDateString,
                     LocalDateTime scheduleEndDateString,
                     String inputCipherPath,
                     String outputCipherPath) throws InvalidCronExpressionException, GenesisException;

    void updateLastExecutionName(String surveyName) throws NotFoundException;
}
