package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public List<KraftwerkExecutionScheduleV2> removeExpiredSchedules(
            DataProcessingContextModel context
    ) {
        LocalDateTime now = LocalDateTime.now();

        List<KraftwerkExecutionScheduleV2> deletedSchedules =
                Optional.ofNullable(context.getKraftwerkExecutionScheduleV2List())
                        .orElse(List.of())
                        .stream()
                        .filter(schedule -> schedule.getScheduleEndDate() != null)
                        .filter(schedule -> schedule.getScheduleEndDate().isBefore(now))
                        .toList();

        for (KraftwerkExecutionScheduleV2 scheduleToRemove : deletedSchedules) {
            Query query = Query.query(
                    Criteria.where("collectionInstrumentId").is(context.getCollectionInstrumentId())
            );

            Update update = new Update().pull(
                    "kraftwerkExecutionScheduleV2List",
                    Query.query(
                            Criteria.where("scheduleUuid").is(scheduleToRemove.getScheduleUuid())
                    ).getQueryObject()
            );
            mongoTemplate.updateMulti(
                    query,
                    update,
                    Constants.MONGODB_CONTEXT_COLLECTION_NAME
            );

        }

        return deletedSchedules;
    }

    @Override
    public List<DataProcessingContextDocument> findAllByReview(boolean withReview) {
        return dataProcessingContextMongoDBRepository.findAll()
                .stream().filter(doc ->
                        doc.isWithReview() == withReview
                ).toList();
    }
}
