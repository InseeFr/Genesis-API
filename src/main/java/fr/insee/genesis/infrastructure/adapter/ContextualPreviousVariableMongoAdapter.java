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

import java.util.List;

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
        List<ContextualPreviousVariableDocument> contextualPreviousVariableDocumentList =
                repository.findByQuestionnaireIdAndInterrogationId(collectionInstrumentId, interrogationId);
        List<ContextualPreviousVariableDocument> docIdentifiedByCollectionInstrumentId =
                repository.findByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);
        if (!docIdentifiedByCollectionInstrumentId.isEmpty()){
            contextualPreviousVariableDocumentList.addAll(docIdentifiedByCollectionInstrumentId);
        }
        if(contextualPreviousVariableDocumentList.isEmpty()){
            return null;
        }
        if(contextualPreviousVariableDocumentList.size() > 1){
            log.warn("More than 1 contextual previous response document for collection instrument {}, interrogation {}", collectionInstrumentId, interrogationId);
        }
        return ContextualPreviousVariableDocumentMapper.INSTANCE.documentToModel(contextualPreviousVariableDocumentList.getFirst());
    }
}
