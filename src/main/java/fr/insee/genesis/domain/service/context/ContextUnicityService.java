package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ContextUnicityService {
    public DataProcessingContextModel deduplicateSchedules(String partitionId,
                                                           List<DataProcessingContextModel> dataProcessingContextModels) {
        if(dataProcessingContextModels.isEmpty()) {
            return null;
        }
        if(dataProcessingContextModels.size() == 1) {
            return dataProcessingContextModels.getFirst();
        }

        log.info("{} survey descriptions found for {}, deduplicating...", dataProcessingContextModels.size(), partitionId);

        DataProcessingContextModel deduplicatedSurveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .withReview(false)
                .build();

        //Add schedule in dedup if doesn't exists already
        for(DataProcessingContextModel dataProcessingContextModel : dataProcessingContextModels){
            for(KraftwerkExecutionSchedule storedExecutionSchedule : dataProcessingContextModel.getKraftwerkExecutionScheduleList()){
                if(deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().isEmpty()){
                    deduplicatedSurveySchedule.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
                if(!deduplicatedSurveySchedule.isWithReview() && dataProcessingContextModel.isWithReview()){
                    deduplicatedSurveySchedule.setWithReview(true);
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
