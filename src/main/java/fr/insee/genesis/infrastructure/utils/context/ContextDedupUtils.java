package fr.insee.genesis.infrastructure.utils.context;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
@Slf4j
public class ContextDedupUtils {
    public static List<DataProcessingContextDocument> deduplicateContexts(List<DataProcessingContextDocument> dataProcessingContextDocuments) {
        List<DataProcessingContextDocument> dedupContextDocuments = new ArrayList<>();

        //Sort contexts per partitionId for dedup
        Map<String, List<DataProcessingContextDocument>> documentsPerPartitionId = new HashMap<>();
        for(DataProcessingContextDocument dataProcessingContextDocument : dataProcessingContextDocuments){
            if(!documentsPerPartitionId.containsKey(dataProcessingContextDocument.getPartitionId())){
                documentsPerPartitionId.put(dataProcessingContextDocument.getPartitionId(), new ArrayList<>());
            }
            documentsPerPartitionId.get(dataProcessingContextDocument.getPartitionId()).add(dataProcessingContextDocument);
        }

        //Dedup for each partition
        for(Map.Entry<String, List<DataProcessingContextDocument>> entry : documentsPerPartitionId.entrySet()){
            dedupContextDocuments.add(deduplicateContexts(entry.getKey(), entry.getValue()));
        }

        return dedupContextDocuments;
    }


    public static DataProcessingContextDocument deduplicateContexts(String partitionId,
                                                                    List<DataProcessingContextDocument> dataProcessingContextDocuments) {
        if(dataProcessingContextDocuments.isEmpty()) {
            return null;
        }
        if(dataProcessingContextDocuments.size() == 1) {
            return dataProcessingContextDocuments.getFirst();
        }

        log.info("{} survey descriptions found for {}, deduplicating...", dataProcessingContextDocuments.size(), partitionId);

        DataProcessingContextModel deduplicatedContext = DataProcessingContextModel.builder()
                .partitionId(partitionId)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .withReview(dataProcessingContextDocuments.getFirst().isWithReview())
                .build();

        //Add schedule in dedup if doesn't exists already
        for(DataProcessingContextDocument dataProcessingContextDocument : dataProcessingContextDocuments){
            if(!dataProcessingContextDocument.isWithReview()){
                deduplicatedContext.setWithReview(false);
            }
            for(KraftwerkExecutionSchedule storedExecutionSchedule : dataProcessingContextDocument.getKraftwerkExecutionScheduleList()){
                if(deduplicatedContext.getKraftwerkExecutionScheduleList().isEmpty()){
                    deduplicatedContext.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
                if(deduplicatedContext.getKraftwerkExecutionScheduleList().stream().filter(
                        schedule -> areSchedulesEquals(schedule,storedExecutionSchedule)).toList().isEmpty()){
                    deduplicatedContext.getKraftwerkExecutionScheduleList().add(storedExecutionSchedule);
                }
            }
        }

        return DataProcessingContextMapper.INSTANCE.modelToDocument(deduplicatedContext);
    }

    private boolean areSchedulesEquals(KraftwerkExecutionSchedule schedule1, KraftwerkExecutionSchedule schedule2){
        return schedule1.getFrequency().equals(schedule2.getFrequency())
                && schedule1.getServiceToCall().equals(schedule2.getServiceToCall())
                && schedule1.getScheduleBeginDate().isEqual(schedule2.getScheduleBeginDate())
                && schedule1.getScheduleEndDate().isEqual(schedule2.getScheduleEndDate());
    }
}
