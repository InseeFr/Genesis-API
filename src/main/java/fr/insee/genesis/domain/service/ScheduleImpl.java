package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleImpl implements ScheduleApiPort {
    private final ScheduleMongoDBRepository scheduleMongoDBRepository;

    @Autowired
    public ScheduleImpl(ScheduleMongoDBRepository scheduleMongoDBRepository) {
        this.scheduleMongoDBRepository = scheduleMongoDBRepository;
    }

    @Override
    public List<ScheduleDocument> getAllSchedules() {
        return scheduleMongoDBRepository.findAll();
    }

    @Override
    public void addSchedule(String surveyName, ServiceToCall serviceToCall, String frequency, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate) {
        List<ScheduleDocument> scheduleDocuments = scheduleMongoDBRepository.findBySurveyName(surveyName);

        ScheduleDocument scheduleDocument;
        if (scheduleDocuments.isEmpty()) {
            //Create if not exists
            scheduleDocuments.add(new ScheduleDocument(surveyName, new ArrayList<>()));
        }//TODO CHECK FREQUENCY
        scheduleDocument = scheduleDocuments.getFirst();
        scheduleDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        frequency,
                        serviceToCall,
                        scheduleBeginDate,
                        scheduleEndDate
                )
        );

        scheduleMongoDBRepository.saveAll(scheduleDocuments);
    }

    @Override
    public void updateLastExecutionName(String surveyName) throws NotFoundException {
        List<ScheduleDocument> scheduleDocuments = scheduleMongoDBRepository.findBySurveyName(surveyName);

        if (!scheduleDocuments.isEmpty()) {
            scheduleDocuments.getFirst().setLastExecution(LocalDateTime.now());
            scheduleMongoDBRepository.saveAll(scheduleDocuments);
        }else{
            throw new NotFoundException();
        }

    }
}
