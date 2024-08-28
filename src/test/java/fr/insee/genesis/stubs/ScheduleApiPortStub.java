package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.service.schedule.ScheduleUnicityService;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.TrustParameters;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;


public class ScheduleApiPortStub implements ScheduleApiPort {

    public List<SurveyScheduleDocument> mongoStub;

    public ScheduleApiPortStub() {
        mongoStub = new ArrayList<>();

        SurveyScheduleDocument surveyScheduleDocumentTest = new SurveyScheduleDocument(
                "TESTSURVEY",
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        surveyScheduleDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        mongoStub.add(surveyScheduleDocumentTest);
    }

    @Override
    public List<SurveyScheduleDocument> getAllSchedules() {
        return mongoStub;
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException {
        if(!CronExpression.isValidExpression(frequency)) throw new InvalidCronExpressionException();

        List<SurveyScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList(); //Equivalent to findBySurveyname

        if(mongoStubFiltered.isEmpty()){
            //Create survey schedule
            SurveyScheduleDocument surveyScheduleDocument = new SurveyScheduleDocument(
                    surveyName,
                    new ArrayList<>()
            );

            //Add execution schedule
            KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                  frequency,
                    serviceToCall,
                    scheduleBeginDate,
                    scheduleEndDate,
                    trustParameters
            );
            surveyScheduleDocument.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

            mongoStub.add(surveyScheduleDocument);
        }else{
            ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
            SurveyScheduleDocument deduplicatedSurveySchedule = scheduleUnicityService.deduplicateSurveySchedules(surveyName,mongoStubFiltered);

            //Add execution schedule
            KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                    frequency,
                    serviceToCall,
                    scheduleBeginDate,
                    scheduleEndDate,
                    trustParameters
            );
            deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

            mongoStub.removeIf(storedSurveySchedule ->
                    storedSurveySchedule.getSurveyName().equals(surveyName)); //deleteBySurveyName

            mongoStub.add(deduplicatedSurveySchedule);
        }
    }

    @Override
    public void deleteSchedule(String surveyName) throws NotFoundException {
        List<SurveyScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(!mongoStubFiltered.isEmpty()) {
            mongoStub.removeIf(storedSurveySchedule ->
                    storedSurveySchedule.getSurveyName().equals(surveyName));
        }else throw new NotFoundException();
    }

    @Override
    public void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException {
        List<SurveyScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(!mongoStubFiltered.isEmpty()) {
            SurveyScheduleDocument surveyScheduleDocument = mongoStubFiltered.getFirst();
            surveyScheduleDocument.setLastExecution(newDate);
        }else throw new NotFoundException();
    }

    @Override
    public long countSchedules() {
        return mongoStub.size();
    }
}
