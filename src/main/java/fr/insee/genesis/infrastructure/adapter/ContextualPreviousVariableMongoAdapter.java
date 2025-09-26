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
    public void backup(String questionnaireId) {
        deleteBackup(questionnaireId);
        MatchOperation match = Aggregation.match(Criteria.where("questionnaireId").is(questionnaireId));
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection(getFormattedCollection(questionnaireId))
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(match, merge);

        mongoTemplate.aggregate(aggregation, "editedPreviousResponses", ContextualPreviousVariableDocument.class);
    }

    private static String getFormattedCollection(String questionnaireId) {
        return "editedPreviousResponses_%s_backup".formatted(questionnaireId);
    }

    @Override
    public void deleteBackup(String questionnaireId) {
        if (mongoTemplate.collectionExists(getFormattedCollection(questionnaireId))){
            mongoTemplate.dropCollection(getFormattedCollection(questionnaireId));
        }
    }

    @Override
    public void restoreBackup(String questionnaireId) {
        delete(questionnaireId);
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedPreviousResponses")
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(merge);

        mongoTemplate.aggregate(aggregation, getFormattedCollection(questionnaireId),
                ContextualPreviousVariableDocument.class);
    }

    @Override
    public void saveAll(List<ContextualPreviousVariableModel> contextualPreviousVariableModelList) {
        repository.saveAll(ContextualPreviousVariableDocumentMapper.INSTANCE.listModelToListDocument(
                contextualPreviousVariableModelList)
        );
    }

    @Override
    public void delete(String questionnaireId) {
        repository.deleteByQuestionnaireId(questionnaireId);
    }

    @Override
    public ContextualPreviousVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId) {
        List<ContextualPreviousVariableDocument> contextualPreviousVariableDocumentList =
                repository.findByQuestionnaireIdAndInterrogationId(questionnaireId, interrogationId);
        if(contextualPreviousVariableDocumentList.isEmpty()){
            return null;
        }
        if(contextualPreviousVariableDocumentList.size() > 1){
            log.warn("More than 1 contextual previous response document for questionnaire {}, interrogation {}", questionnaireId, interrogationId);
        }
        return ContextualPreviousVariableDocumentMapper.INSTANCE.documentToModel(contextualPreviousVariableDocumentList.getFirst());
    }
}
