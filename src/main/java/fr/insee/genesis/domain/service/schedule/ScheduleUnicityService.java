package fr.insee.genesis.domain.service.schedule;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduleUnicityService {
    public ScheduleModel deduplicateSurveySchedules(String surveyName, List<ScheduleModel> scheduleModels) {
        if(scheduleModels.isEmpty()) {
            return null;
        }
        if(scheduleModels.size() == 1) {
            return scheduleModels.getFirst();
        }

        log.info("{} survey descriptions found for {}, deduplicating...", scheduleModels.size(), surveyName);

        ScheduleModel deduplicatedSurveySchedule = ScheduleModel.builder()
                        .surveyName(surveyName)
                        .kraftwerkExecutionScheduleList(new ArrayList<>())
                        .build();

        //Add schedule in dedup if doesn't exists already
        for(ScheduleModel scheduleModel : scheduleModels){
            for(KraftwerkExecutionSchedule storedExecutionSchedule : scheduleModel.getKraftwerkExecutionScheduleList()){
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
