package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.utils.context.ContextDedupUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class DataProcessingContextPersistancePortStub implements DataProcessingContextPersistancePort {
    List<DataProcessingContextDocument> mongoStub = new ArrayList<>();

    @Override
    public DataProcessingContextDocument findByPartitionId(String partitionId) {
        return ContextDedupUtils.deduplicateContexts(partitionId,
                mongoStub.stream().filter(
                        dataProcessingContextDocument -> dataProcessingContextDocument.getPartitionId().equals(partitionId)
                ).toList()
        );
    }

    @Override
    public List<DataProcessingContextModel> findByPartitionIds(List<String> partitionIds) {
        return DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                ContextDedupUtils.deduplicateContexts(
                        mongoStub.stream().filter(
                            dataProcessingContextDocument -> partitionIds.contains(dataProcessingContextDocument.getPartitionId())
                        ).toList()
                )
        );
    }

    @Override
    public void save(DataProcessingContextDocument dataProcessingContextDocument) {
        mongoStub.removeIf(existingDoc -> existingDoc.getPartitionId().equals(
                dataProcessingContextDocument.getPartitionId())
        );
        mongoStub.add(dataProcessingContextDocument);
    }

    @Override
    public void saveAll(List<DataProcessingContextDocument> dataProcessingContextDocuments) {
        mongoStub.addAll(dataProcessingContextDocuments);
    }

    @Override
    public void deleteBypartitionId(String partitionId) {
        mongoStub.removeIf(doc -> doc.getPartitionId().equals(partitionId));
    }

    @Override
    public List<DataProcessingContextDocument> findAll() {
        return mongoStub;
    }

    @Override
    public long count() {
        return mongoStub.size();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(DataProcessingContextModel dataProcessingContextModel) {
        List<KraftwerkExecutionSchedule> kraftwerkExecutionSchedulesToRemove = new ArrayList<>(dataProcessingContextModel.getKraftwerkExecutionScheduleList().stream().filter(
                kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
        ).toList());
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove : kraftwerkExecutionSchedulesToRemove){
            dataProcessingContextModel.getKraftwerkExecutionScheduleList().remove(kraftwerkExecutionScheduleToRemove);
            log.info("Removed kraftwerk execution schedule on {} because it is expired since {}",
                    dataProcessingContextModel.getPartitionId(),
                    kraftwerkExecutionScheduleToRemove.getScheduleEndDate());
        }
        //Update mongo stub
        mongoStub.removeIf(scheduleDocument -> scheduleDocument.getPartitionId().equals(dataProcessingContextModel.getPartitionId()));
        mongoStub.add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
        return kraftwerkExecutionSchedulesToRemove;
    }

    @Override
    public List<DataProcessingContextDocument> findAllByReview(boolean withReview) {
        return mongoStub.stream().filter(doc -> doc.isWithReview() == withReview).toList();
    }
}
