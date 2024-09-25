package fr.insee.genesis.domain.service.schedule;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
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
    public List<ScheduleModel> getAllSchedules() {
        return schedulePersistencePort.getAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency,
                            LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException{
        //Frequency format check
        if(!CronExpression.isValidExpression(frequency)) {
            throw new InvalidCronExpressionException();
        }
        
        List<ScheduleModel> scheduleModels = schedulePersistencePort.findBySurveyName(surveyName);

        ScheduleModel scheduleModel;
        if (scheduleModels.isEmpty()) {
            //Create if not exists
            log.info("Creation of new survey document for survey {}", surveyName);
            scheduleModels.add(ScheduleModel.builder()
                    .surveyName(surveyName)
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build());
        }
        ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
        scheduleModel = scheduleUnicityService.deduplicateSurveySchedules(surveyName, scheduleModels);
        scheduleModels.clear();
        scheduleModels.add(scheduleModel);
        scheduleModel.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        frequency,
                        serviceToCall,
                        scheduleBeginDate,
                        scheduleEndDate,
                        trustParameters
                )
        );

        schedulePersistencePort.deleteBySurveyName(surveyName);
        schedulePersistencePort.saveAll(scheduleModels);
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
        List<ScheduleModel> scheduleModels = schedulePersistencePort.findBySurveyName(surveyName);
        if (scheduleModels.isEmpty()) {
            throw new NotFoundException();
        }
        for(ScheduleModel surveySchedule : scheduleModels){
            surveySchedule.setLastExecution(newDate);
        }
        schedulePersistencePort.deleteBySurveyName(surveyName);
        schedulePersistencePort.saveAll(scheduleModels);
    }

    @Override
    public long countSchedules() {
        return schedulePersistencePort.countSchedules();
    }
}
