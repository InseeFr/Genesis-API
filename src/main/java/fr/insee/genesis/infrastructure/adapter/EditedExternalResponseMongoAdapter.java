package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.editedexternal.EditedExternalResponseModel;
import fr.insee.genesis.domain.ports.spi.EditedExternalResponsePersistancePort;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import fr.insee.genesis.infrastructure.mappers.EditedExternalResponseDocumentMapper;
import fr.insee.genesis.infrastructure.repository.EditedExternalResponseMongoDBRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("editedExternalResponseMongoAdapter")
public class EditedExternalResponseMongoAdapter implements EditedExternalResponsePersistancePort {
    private final MongoTemplate mongoTemplate;
    private final EditedExternalResponseMongoDBRepository repository;

    public EditedExternalResponseMongoAdapter(EditedExternalResponseMongoDBRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void backup(String questionnaireId) {
        deleteBackup(questionnaireId);
        MatchOperation match = Aggregation.match(Criteria.where("questionnaireId").is(questionnaireId));
        MergeOperation merge = Aggregation
                .merge()
                .intoCollection("editedExternalResponses_%s_backup".formatted(questionnaireId))
                .whenMatched(MergeOperation.WhenDocumentsMatch.replaceDocument())
                .whenDocumentsDontMatch(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                .build();

        Aggregation aggregation = Aggregation.newAggregation(match, merge);

        mongoTemplate.aggregate(aggregation, "editedExternalResponses", EditedExternalResponseDocument.class);
    }

    @Override
    public void deleteBackup(String questionnaireId) {
        if (mongoTemplate.collectionExists("editedExternalResponses_%s_backup".formatted(questionnaireId))){
            mongoTemplate.dropCollection("editedExternalResponses_%s_backup".formatted(questionnaireId));
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

        mongoTemplate.aggregate(aggregation, "editedExternalResponses_%s_backup".formatted(questionnaireId),
                EditedExternalResponseDocument.class);
    }

    @Override
    public void saveAll(List<EditedExternalResponseModel> editedExternalResponseModelList) {
        repository.saveAll(EditedExternalResponseDocumentMapper.INSTANCE.listModelToListDocument(
                editedExternalResponseModelList)
        );
    }

    @Override
    public void delete(String questionnaireId) {
        repository.deleteByQuestionnaireId(questionnaireId);
    }
}
