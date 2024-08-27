package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.TrustParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleService implements ScheduleApiPort {
    private final SchedulePersistencePort schedulePersistencePort;

    @Autowired
    public ScheduleService(SchedulePersistencePort schedulePersistencePort) {
        this.schedulePersistencePort = schedulePersistencePort;
    }

    @Override
    public List<StoredSurveySchedule> getAllSchedules() {
        return schedulePersistencePort.getAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency,
                            LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException{
        //Frequency format check
        if(!CronExpression.isValidExpression(frequency)) {
            throw new InvalidCronExpressionException();
        }
        
        List<StoredSurveySchedule> storedSurveySchedules =
                new ArrayList<>(schedulePersistencePort.findBySurveyName(surveyName));

        StoredSurveySchedule storedSurveySchedule;
        if (storedSurveySchedules.isEmpty()) {
            //Create if not exists
            log.info("Creation of new survey document for survey {}", surveyName);
            storedSurveySchedules.add(new StoredSurveySchedule(surveyName, new ArrayList<>()));
        }
        ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
        storedSurveySchedule = scheduleUnicityService.deduplicateSurveySchedules(surveyName, storedSurveySchedules);
        storedSurveySchedules.clear();
        storedSurveySchedules.add(storedSurveySchedule);
        storedSurveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        frequency,
                        serviceToCall,
                        scheduleBeginDate,
                        scheduleEndDate,
                        trustParameters
                )
        );

        schedulePersistencePort.deleteBySurveyName(surveyName);
        schedulePersistencePort.saveAll(storedSurveySchedules);
    }

    @Override
    public void deleteSchedule(String surveyName) throws NotFoundException {
        if(schedulePersistencePort.findBySurveyName(surveyName).isEmpty()){
            throw new NotFoundException();
        }
        schedulePersistencePort.deleteBySurveyName(surveyName);
    }

    @Override
    public void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException {
        List<StoredSurveySchedule> storedSurveySchedules = schedulePersistencePort.findBySurveyName(surveyName);
        if (storedSurveySchedules.isEmpty()) {
            throw new NotFoundException();
        }
        for(StoredSurveySchedule surveySchedule : storedSurveySchedules){
            surveySchedule.setLastExecution(newDate);
        }
        schedulePersistencePort.saveAll(storedSurveySchedules);
    }

    @Override
    public long countSchedules() {
        return schedulePersistencePort.countSchedules();
    }
}
