package fr.insee.genesis.domain.service;

import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleUnicityService {
    public StoredSurveySchedule deduplicateSurveySchedules(String surveyName, List<StoredSurveySchedule> storedSurveySchedules) {
        if(storedSurveySchedules.isEmpty()) {
            return null;
        }
        if(storedSurveySchedules.size() == 1) {
            return storedSurveySchedules.getFirst();
        }

        log.info(storedSurveySchedules.size() + " survey descriptions found for " + surveyName + ", deduplicating...");

        StoredSurveySchedule deduplicatedSurveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );

        //Add schedule in dedup if doesn't exists already
        for(StoredSurveySchedule storedSurveySchedule : storedSurveySchedules){
            for(KraftwerkExecutionSchedule storedExecutionSchedule : storedSurveySchedule.getKraftwerkExecutionScheduleList()){
                if(deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().isEmpty()){
                    deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
                if(deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().stream().filter(
                        schedule -> areSchedulesEquals(schedule,storedExecutionSchedule)
                ).toList().isEmpty()){
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
