package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.service.ScheduleUnicityService;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;


public class ScheduleApiPortStub implements ScheduleApiPort {

    public List<StoredSurveySchedule> mongoStub;

    public ScheduleApiPortStub() {
        mongoStub = new ArrayList<>();

        StoredSurveySchedule storedSurveyScheduleTest = new StoredSurveySchedule(
                "TESTSURVEY",
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1)
        );
        storedSurveyScheduleTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        mongoStub.add(storedSurveyScheduleTest);
    }

    @Override
    public List<StoredSurveySchedule> getAllSchedules() {
        return mongoStub;
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate) throws InvalidCronExpressionException {
        if(!CronExpression.isValidExpression(frequency)) throw new InvalidCronExpressionException();

        List<StoredSurveySchedule> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList(); //Equivalent to findBySurveyname

        if(mongoStubFiltered.isEmpty()){
            //Create survey schedule
            StoredSurveySchedule storedSurveySchedule = new StoredSurveySchedule(
                    surveyName,
                    new ArrayList<>()
            );

            //Add execution schedule
            KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                  frequency,
                    serviceToCall,
                    scheduleBeginDate,
                    scheduleEndDate
            );
            storedSurveySchedule.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

            mongoStub.add(storedSurveySchedule);
        }else{
            ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
            StoredSurveySchedule deduplicatedSurveySchedule = scheduleUnicityService.deduplicateSurveySchedules(surveyName,mongoStubFiltered);

            //Add execution schedule
            KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                    frequency,
                    serviceToCall,
                    scheduleBeginDate,
                    scheduleEndDate
            );
            deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

            mongoStub.removeIf(storedSurveySchedule ->
                    storedSurveySchedule.getSurveyName().equals(surveyName)); //deleteBySurveyName

            mongoStub.add(deduplicatedSurveySchedule);
        }
    }

    @Override
    public void updateLastExecutionName(String surveyName) throws NotFoundException {
        List<StoredSurveySchedule> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(!mongoStubFiltered.isEmpty()) {
            StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
            storedSurveySchedule.setLastExecution(LocalDateTime.now());
        }else throw new NotFoundException();
    }
}