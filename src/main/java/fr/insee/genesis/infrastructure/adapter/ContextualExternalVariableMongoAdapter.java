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
    public void restoreBackup(String questionnaireId) {
        delete(questionnaireId);
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedExternalResponses")
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(merge);

        mongoTemplate.aggregate(aggregation, getFormattedCollection(questionnaireId),
                ContextualExternalVariableDocument.class);
    }

    @Override
    public void saveAll(List<ContextualExternalVariableModel> contextualExternalVariableModelList) {
        repository.saveAll(ContextualExternalVariableDocumentMapper.INSTANCE.listModelToListDocument(
                contextualExternalVariableModelList)
        );
    }

    @Override
    public void delete(String questionnaireId) {
        repository.deleteByQuestionnaireId(questionnaireId);
    }

    @Override
    public ContextualExternalVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId) {
        List<ContextualExternalVariableDocument> contextualExternalVariableDocumentList =
                repository.findByQuestionnaireIdAndInterrogationId(questionnaireId, interrogationId);
        if(contextualExternalVariableDocumentList.isEmpty()){
            return null;
        }
        if(contextualExternalVariableDocumentList.size() > 1){
            log.warn("More than 1 contextual external response document for questionnaire {}, interrogation {}", questionnaireId, interrogationId);
        }
        return ContextualExternalVariableDocumentMapper.INSTANCE.documentToModel(contextualExternalVariableDocumentList.getFirst());
    }
}
