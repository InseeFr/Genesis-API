package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualExternalVariablePersistancePort;
import fr.insee.genesis.infrastructure.document.contextualexternal.ContextualExternalVariableDocument;
import fr.insee.genesis.infrastructure.mappers.ContextualExternalVariableDocumentMapper;
import fr.insee.genesis.infrastructure.repository.ContextualExternalVariableMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Qualifier("contextualExternalVariableMongoAdapter")
public class ContextualExternalVariableMongoAdapter implements ContextualExternalVariablePersistancePort {
    private final MongoTemplate mongoTemplate;
    private final ContextualExternalVariableMongoDBRepository repository;

    public ContextualExternalVariableMongoAdapter(ContextualExternalVariableMongoDBRepository repository, MongoTemplate mongoTemplate) {
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

        mongoTemplate.aggregate(aggregation, "editedExternalResponses", ContextualExternalVariableDocument.class);
    }

    private static String getFormattedCollection(String questionnaireId) {
        return "editedExternalResponses_%s_backup".formatted(questionnaireId);
    }

    @Override
    public void deleteBackup(String questionnaireId) {
        if (mongoTemplate.collectionExists(getFormattedCollection(questionnaireId))){
            mongoTemplate.dropCollection(getFormattedCollection(questionnaireId));
        }
    }

    @Override
    public void restoreBackup(String collectionInstrumentId) {
        delete(collectionInstrumentId);
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedExternalResponses")
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(merge);

        mongoTemplate.aggregate(aggregation, getFormattedCollection(collectionInstrumentId),
                ContextualExternalVariableDocument.class);
    }

    @Override
    public void saveAll(List<ContextualExternalVariableModel> contextualExternalVariableModelList) {
        repository.saveAll(ContextualExternalVariableDocumentMapper.INSTANCE.listModelToListDocument(
                contextualExternalVariableModelList)
        );
    }

    @Override
    public void delete(String collectionInstrumentId) {
        repository.deleteByCollectionInstrumentId(collectionInstrumentId);
        // For older documents
        repository.deleteByQuestionnaireId(collectionInstrumentId);
    }

    @Override
    public ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId) {
        List<ContextualExternalVariableDocument> results = new ArrayList<>();
        results.addAll(repository.findByQuestionnaireIdAndInterrogationId(collectionInstrumentId, interrogationId));
        // For older documents
        results.addAll(repository.findByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId));
        if(results.isEmpty()){
            return null;
        }
        if(results.size() > 1){
            log.warn("More than 1 contextual external response document for collection instrument {}, interrogation {}", collectionInstrumentId, interrogationId);
        }
        return ContextualExternalVariableDocumentMapper.INSTANCE.documentToModel(results.getFirst());
    }
}
