package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleImpl implements ScheduleApiPort {
    private final ScheduleMongoDBRepository scheduleMongoDBRepository;

    @Autowired
    public ScheduleImpl(ScheduleMongoDBRepository scheduleMongoDBRepository) {
        this.scheduleMongoDBRepository = scheduleMongoDBRepository;
    }

    @Override
    public List<StoredSurveySchedule> getAllSchedules() {
        return scheduleMongoDBRepository.findAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate) throws InvalidCronExpressionException{
        //Frequency format check
        if(!CronExpression.isValidExpression(frequency)) {
            throw new InvalidCronExpressionException();
        }
        
        List<StoredSurveySchedule> storedSurveySchedules = scheduleMongoDBRepository.findBySurveyName(surveyName);

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
                        scheduleEndDate
                )
        );
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
        scheduleMongoDBRepository.saveAll(storedSurveySchedules);
    }

    @Override
    public void deleteSchedule(String surveyName) throws NotFoundException {
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
    }

    @Override
    public void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException {
        List<StoredSurveySchedule> storedSurveySchedules = scheduleMongoDBRepository.findBySurveyName(surveyName);

        if (!storedSurveySchedules.isEmpty()) {
            for(StoredSurveySchedule surveySchedule : storedSurveySchedules){
                surveySchedule.setLastExecution(newDate);
            }
            scheduleMongoDBRepository.saveAll(storedSurveySchedules);
        }else{
            throw new NotFoundException();
        }



    }

    @Override
    public long countSchedules() {
        return scheduleMongoDBRepository.count();
    }
}
