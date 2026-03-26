package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public DataProcessingContextMongoAdapter(
            DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository,
            MongoTemplate mongoTemplate
    ) {
        this.dataProcessingContextMongoDBRepository = dataProcessingContextMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public DataProcessingContextModel findByCollectionInstrumentId(String collectionInstrumentId) {
        List<DataProcessingContextDocument> existingDocuments =
                dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(List.of(collectionInstrumentId));
        return DataProcessingContextMapper.INSTANCE.documentToModel(
                existingDocuments.isEmpty() ? null : existingDocuments.getFirst()
        );
    }

    @Override
    public List<DataProcessingContextModel> findByCollectionInstrumentIds(List<String> collectionInstrumentIds) {
        List<DataProcessingContextDocument> existingDocuments =
                dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(collectionInstrumentIds);
        return DataProcessingContextMapper.INSTANCE.listDocumentToListModel(existingDocuments);
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
    public List<DataProcessingContextDocument> findAll() {
        return dataProcessingContextMongoDBRepository.findAll();
    }

    @Override
    public long count() {
        return dataProcessingContextMongoDBRepository.count();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(DataProcessingContextModel dataProcessingContextModel) throws IOException {
        //TODO move non mongo related logic to service
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove :
                dataProcessingContextModel.getKraftwerkExecutionScheduleList().stream().filter(
                        kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
                ).toList()) {
            deletedKraftwerkExecutionSchedules.add(kraftwerkExecutionScheduleToRemove);
            Query query =
                    Query.query(Criteria.where("scheduleEndDate").is(kraftwerkExecutionScheduleToRemove.getScheduleEndDate()));
            if (dataProcessingContextModel.getCollectionInstrumentId() != null){
                mongoTemplate.updateMulti(Query.query(Criteria.where("collectionInstrumentId").is(dataProcessingContextModel.getCollectionInstrumentId())), new Update().pull(
                                "kraftwerkExecutionScheduleList", query),
                        Constants.MONGODB_SCHEDULE_COLLECTION_NAME);
            }
        }
        return deletedKraftwerkExecutionSchedules;
    }

    @Override
    public List<DataProcessingContextDocument> findAllByReview(boolean withReview) {
        return dataProcessingContextMongoDBRepository.findAll()
                .stream().filter(doc ->
                        doc.isWithReview() == withReview
                ).toList();
    }
}
