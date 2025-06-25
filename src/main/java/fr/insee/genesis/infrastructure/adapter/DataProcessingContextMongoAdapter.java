package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import fr.insee.genesis.infrastructure.utils.context.ContextDedupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Qualifier("dataProcessingContextMongoAdapter")
@Slf4j
public class DataProcessingContextMongoAdapter implements DataProcessingContextPersistancePort {
    private final DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DataProcessingContextMongoAdapter(DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository, MongoTemplate mongoTemplate) {
        this.dataProcessingContextMongoDBRepository = dataProcessingContextMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public DataProcessingContextDocument findByPartitionId(String partitionId) {
        List<DataProcessingContextDocument> existingDocuments =
                dataProcessingContextMongoDBRepository.findByPartitionIdList(List.of(partitionId));
        return ContextDedupUtils.deduplicateContexts(partitionId, existingDocuments);
    }

    @Override
    public List<DataProcessingContextModel> findByPartitionIds(List<String> partitionIds){
        List<DataProcessingContextDocument> existingDocuments =
                dataProcessingContextMongoDBRepository.findByPartitionIdList(partitionIds);
        return DataProcessingContextMapper.INSTANCE.listDocumentToListModel(ContextDedupUtils.deduplicateContexts(existingDocuments));
    }

    @Override
    public void save(DataProcessingContextDocument dataProcessingContextDocument) {
        dataProcessingContextMongoDBRepository.save(dataProcessingContextDocument);
    }

    @Override
    public void saveAll(List<DataProcessingContextDocument> dataProcessingContextDocuments) {
        dataProcessingContextMongoDBRepository.saveAll(dataProcessingContextDocuments);
    }

    @Override
    public void deleteBypartitionId(String partitionId) {
        dataProcessingContextMongoDBRepository.deleteByPartitionId(partitionId);
    }

    @Override
    public List<DataProcessingContextDocument> findAll() {
        return ContextDedupUtils.deduplicateContexts(dataProcessingContextMongoDBRepository.findAll());
    }

    @Override
    public long count() {
        return dataProcessingContextMongoDBRepository.count();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(DataProcessingContextModel dataProcessingContextModel) {
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove :
                dataProcessingContextModel.getKraftwerkExecutionScheduleList().stream().filter(
                        kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
                ).toList()) {
            deletedKraftwerkExecutionSchedules.add(kraftwerkExecutionScheduleToRemove);
            Query query =
                    Query.query(Criteria.where("scheduleEndDate").is(kraftwerkExecutionScheduleToRemove.getScheduleEndDate()));
            mongoTemplate.updateMulti(Query.query(Criteria.where("surveyName").is(dataProcessingContextModel.getPartitionId())), new Update().pull(
                            "kraftwerkExecutionScheduleList", query),
                    Constants.MONGODB_SCHEDULE_COLLECTION_NAME);
            log.info("Removed kraftwerk execution schedule on {} because it is expired since {}", dataProcessingContextModel.getPartitionId(),
                    kraftwerkExecutionScheduleToRemove.getScheduleEndDate());
        }
        return deletedKraftwerkExecutionSchedules;
    }
}
