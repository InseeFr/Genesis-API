package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.service.schedule.ScheduleUnicityService;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;


public class ScheduleApiPortStub implements ScheduleApiPort {

    public List<ScheduleDocument> mongoStub;

    public ScheduleApiPortStub() {
        mongoStub = new ArrayList<>();

        ScheduleDocument scheduleDocumentTest = new ScheduleDocument(
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
        scheduleDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        mongoStub.add(scheduleDocumentTest);
    }

    @Override
    public List<ScheduleModel> getAllSchedules() {
        return ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub);
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate, TrustParameters trustParameters) throws InvalidCronExpressionException {
        if(!CronExpression.isValidExpression(frequency)) throw new InvalidCronExpressionException();

        List<ScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList(); //Equivalent to findBySurveyname

        if(mongoStubFiltered.isEmpty()){
            //Create survey schedule
            ScheduleDocument scheduleDocument = new ScheduleDocument(
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
            scheduleDocument.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

            mongoStub.add(scheduleDocument);
        }else{
            ScheduleUnicityService scheduleUnicityService = new ScheduleUnicityService();
            ScheduleModel deduplicatedSurveySchedule = scheduleUnicityService.deduplicateSurveySchedules(surveyName,
                    ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(mongoStubFiltered));

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

            mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(deduplicatedSurveySchedule));
        }
    }

    @Override
    public void deleteSchedule(String surveyName) throws NotFoundException {
        List<ScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(!mongoStubFiltered.isEmpty()) {
            mongoStub.removeIf(storedSurveySchedule ->
                    storedSurveySchedule.getSurveyName().equals(surveyName));
        }else throw new NotFoundException();
    }

    @Override
    public List<KraftwerkExecutionSchedule> deleteExpiredSchedules(String surveyName) throws NotFoundException {
        List<ScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(mongoStubFiltered.isEmpty()){
            throw new NotFoundException();
        }
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for(ScheduleDocument scheduleDocument : mongoStubFiltered){
            deletedKraftwerkExecutionSchedules.addAll(removeExpiredSchedules(scheduleDocument));
            if(scheduleDocument.getKraftwerkExecutionScheduleList().isEmpty()){
                mongoStub.remove(scheduleDocument);
            }
        }
        return deletedKraftwerkExecutionSchedules;
    }

    @Override
    public void updateLastExecutionName(String surveyName, LocalDateTime newDate) throws NotFoundException {
        List<ScheduleDocument> mongoStubFiltered = mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();
        if(!mongoStubFiltered.isEmpty()) {
            ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();
            scheduleDocument.setLastExecution(newDate);
        }else throw new NotFoundException();
    }

    @Override
    public long countSchedules() {
        return mongoStub.size();
    }

    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(ScheduleDocument scheduleDocument) {
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>(
                scheduleDocument.getKraftwerkExecutionScheduleList().stream().filter(
                kraftwerkExecutionSchedule1 ->
                        kraftwerkExecutionSchedule1.getScheduleEndDate().isBefore(LocalDateTime.now())).toList()
        );

        scheduleDocument.getKraftwerkExecutionScheduleList().removeIf(
                kraftwerkExecutionSchedule1 ->
                        kraftwerkExecutionSchedule1.getScheduleEndDate().isBefore(LocalDateTime.now())
        );
        return deletedKraftwerkExecutionSchedules;
    }
}
