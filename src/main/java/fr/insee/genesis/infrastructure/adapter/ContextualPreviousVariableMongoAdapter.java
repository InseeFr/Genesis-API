package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualPreviousVariablePersistancePort;
import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import fr.insee.genesis.infrastructure.mappers.ContextualPreviousVariableDocumentMapper;
import fr.insee.genesis.infrastructure.repository.ContextualPreviousVariableMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Qualifier("contextualPreviousVariableMongoAdapter")
public class ContextualPreviousVariableMongoAdapter implements ContextualPreviousVariablePersistancePort {
    private final MongoTemplate mongoTemplate;
    private final ContextualPreviousVariableMongoDBRepository repository;

    public ContextualPreviousVariableMongoAdapter(ContextualPreviousVariableMongoDBRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void backup(String collectionInstrumentId) {
        deleteBackup(collectionInstrumentId);
        MatchOperation match = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("questionnaireId").is(collectionInstrumentId),
                        Criteria.where("collectionInstrumentId").is(collectionInstrumentId)
                )
        );
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection(getFormattedCollection(collectionInstrumentId))
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(match, merge);

        mongoTemplate.aggregate(aggregation, "editedPreviousResponses", ContextualPreviousVariableDocument.class);
    }

    private static String getFormattedCollection(String collectionInstrumentId) {
        return "editedPreviousResponses_%s_backup".formatted(collectionInstrumentId);
    }

    @Override
    public void deleteBackup(String collectionInstrumentId) {
        if (mongoTemplate.collectionExists(getFormattedCollection(collectionInstrumentId))){
            mongoTemplate.dropCollection(getFormattedCollection(collectionInstrumentId));
        }
    }

    @Override
    public void restoreBackup(String collectionInstrumentId) {
        delete(collectionInstrumentId);
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedPreviousResponses")
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(merge);

        mongoTemplate.aggregate(aggregation, getFormattedCollection(collectionInstrumentId),
                ContextualPreviousVariableDocument.class);
    }

    @Override
    public void saveAll(List<ContextualPreviousVariableModel> contextualPreviousVariableModelList) {
        repository.saveAll(ContextualPreviousVariableDocumentMapper.INSTANCE.listModelToListDocument(
                contextualPreviousVariableModelList)
        );
    }

    @Override
    public void delete(String collectionInstrumentId) {
        repository.deleteByCollectionInstrumentId(collectionInstrumentId);
        repository.deleteByQuestionnaireId(collectionInstrumentId);
    }

    @Override
    public ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId) {
        List<ContextualPreviousVariableDocument> results = new ArrayList<>();
        results.addAll(repository.findByQuestionnaireIdAndInterrogationId(collectionInstrumentId, interrogationId));
        results.addAll(repository.findByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId));
        if(results.isEmpty()){
            return null;
        }
        if(results.size() > 1){
            log.warn("More than 1 contextual previous response document for collection instrument {}, interrogation {}", collectionInstrumentId, interrogationId);
        }
        return ContextualPreviousVariableDocumentMapper.INSTANCE.documentToModel(results.getFirst());
    }

    @Override
    public Map<String, ContextualPreviousVariableModel> findByCollectionInstrumentIdAndInterrogationIdList(String collectionInstrumentId, List<String> interrogationIds) {
        Map<String, ContextualPreviousVariableModel> contextualPreviousVariableModelMap = new HashMap<>();

        List<ContextualPreviousVariableDocument> results = new ArrayList<>();
        results.addAll(repository.findByQuestionnaireIdAndInterrogationIdList(collectionInstrumentId, interrogationIds));
        // For older documents
        results.addAll(repository.findByCollectionInstrumentIdAndInterrogationIdList(collectionInstrumentId, interrogationIds));

        for(ContextualPreviousVariableDocument contextualPreviousVariableDocument : results){
            String docInterrogationId = contextualPreviousVariableDocument.getInterrogationId();
            if(docInterrogationId == null || docInterrogationId.isEmpty())
            {
                continue;
            }
            contextualPreviousVariableModelMap.put(
                    docInterrogationId,
                    ContextualPreviousVariableDocumentMapper.INSTANCE.documentToModel(contextualPreviousVariableDocument)
            );
        }

        return contextualPreviousVariableModelMap;
    }
}
