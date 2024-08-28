package fr.insee.genesis.domain.service.schedule;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
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
    public List<SurveyScheduleDocument> getAllSchedules() {
        return schedulePersistencePort.getAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency,
                            LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException{
        //Frequency format check
        if(!CronExpression.isValidExpression(frequency)) {
            throw new InvalidCronExpressionException();
        }
        
        List<SurveyScheduleDocument> surveyScheduleDocuments =
                new ArrayList<>(schedulePersistencePort.findBySurveyName(surveyName));

        SurveyScheduleDocument surveyScheduleDocument;
        if (surveyScheduleDocuments.isEmpty()) {
            //Create if not exists
            log.info("Creation of new survey document for survey {}", surveyName);
            surveyScheduleDocuments.add(new SurveyScheduleDocument(surveyName, new ArrayList<>()));
        }
        ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
        surveyScheduleDocument = scheduleUnicityService.deduplicateSurveySchedules(surveyName, surveyScheduleDocuments);
        surveyScheduleDocuments.clear();
        surveyScheduleDocuments.add(surveyScheduleDocument);
        surveyScheduleDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        frequency,
                        serviceToCall,
                        scheduleBeginDate,
                        scheduleEndDate,
                        trustParameters
                )
        );

        schedulePersistencePort.deleteBySurveyName(surveyName);
        schedulePersistencePort.saveAll(surveyScheduleDocuments);
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
        List<SurveyScheduleDocument> surveyScheduleDocuments = schedulePersistencePort.findBySurveyName(surveyName);
        if (surveyScheduleDocuments.isEmpty()) {
            throw new NotFoundException();
        }
        for(SurveyScheduleDocument surveySchedule : surveyScheduleDocuments){
            surveySchedule.setLastExecution(newDate);
        }
        schedulePersistencePort.saveAll(surveyScheduleDocuments);
    }

    @Override
    public long countSchedules() {
        return schedulePersistencePort.countSchedules();
    }
}
