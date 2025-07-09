package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.editedprevious.EditedPreviousResponseModel;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.infrastructure.mappers.EditedPreviousResponseDocumentMapper;
import fr.insee.genesis.infrastructure.repository.EditedPreviousResponseMongoDBRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("editedPreviousResponseMongoAdapter")
public class EditedPreviousResponseMongoAdapter implements EditedPreviousResponsePersistancePort {
    private final MongoTemplate mongoTemplate;
    private final EditedPreviousResponseMongoDBRepository repository;

    public EditedPreviousResponseMongoAdapter(EditedPreviousResponseMongoDBRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void backup(String questionnaireId) {
        deleteBackup(questionnaireId);
        MatchOperation match = Aggregation.match(Criteria.where("questionnaireId").is(questionnaireId));
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedPreviousResponses_%s_backup".formatted(questionnaireId))
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(match, merge);

        mongoTemplate.aggregate(aggregation, "editedPreviousResponses", EditedPreviousResponseDocument.class);
    }

    @Override
    public void deleteBackup(String questionnaireId) {
        if (mongoTemplate.collectionExists("editedPreviousResponses_%s_backup".formatted(questionnaireId))){
            mongoTemplate.dropCollection("editedPreviousResponses_%s_backup".formatted(questionnaireId));
        }
    }

    @Override
    public void restoreBackup(String questionnaireId) {
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedPreviousResponses")
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(merge);

        mongoTemplate.aggregate(aggregation, "editedPreviousResponses_%s_backup".formatted(questionnaireId),
                EditedPreviousResponseDocument.class);
    }

    @Override
    public void saveAll(List<EditedPreviousResponseModel> editedPreviousResponseModelList) {
        repository.saveAll(EditedPreviousResponseDocumentMapper.INSTANCE.listModelToListDocument(
                editedPreviousResponseModelList)
        );
    }

    @Override
    public void delete(String questionnaireId) {
        repository.deleteByQuestionnaireId(questionnaireId);
    }
}
