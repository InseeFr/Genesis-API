package fr.insee.genesis.domain.service.schedule;

import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleUnicityService {
    public SurveyScheduleDocument deduplicateSurveySchedules(String surveyName, List<SurveyScheduleDocument> surveyScheduleDocuments) {
        if(surveyScheduleDocuments.isEmpty()) {
            return null;
        }
        if(surveyScheduleDocuments.size() == 1) {
            return surveyScheduleDocuments.getFirst();
        }

        log.info("{} survey descriptions found for {}, deduplicating...", surveyScheduleDocuments.size(), surveyName);

        SurveyScheduleDocument deduplicatedSurveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );

        //Add schedule in dedup if doesn't exists already
        for(SurveyScheduleDocument surveyScheduleDocument : surveyScheduleDocuments){
            for(KraftwerkExecutionSchedule storedExecutionSchedule : surveyScheduleDocument.getKraftwerkExecutionScheduleList()){
                if(deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().isEmpty()){
                    deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
                if(deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().stream().filter(
                        schedule -> areSchedulesEquals(schedule,storedExecutionSchedule)).toList().isEmpty()){
                    deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
            }
        }


        return deduplicatedSurveySchedule;
    }

    private boolean areSchedulesEquals(KraftwerkExecutionSchedule schedule1, KraftwerkExecutionSchedule schedule2){
        return schedule1.getFrequency().equals(schedule2.getFrequency())
                && schedule1.getServiceToCall().equals(schedule2.getServiceToCall())
                && schedule1.getScheduleBeginDate().isEqual(schedule2.getScheduleBeginDate())
                && schedule1.getScheduleEndDate().isEqual(schedule2.getScheduleEndDate());
    }
}
