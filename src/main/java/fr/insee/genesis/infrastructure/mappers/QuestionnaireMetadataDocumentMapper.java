package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface QuestionnaireMetadataDocumentMapper {
    QuestionnaireMetadataDocumentMapper INSTANCE = Mappers.getMapper(QuestionnaireMetadataDocumentMapper.class);

    QuestionnaireMetadataModel documentToModel(QuestionnaireMetadataDocument document);

    QuestionnaireMetadataDocument modelToDocument(QuestionnaireMetadataModel model);

    List<QuestionnaireMetadataModel> listDocumentToListModel(List<QuestionnaireMetadataDocument> documentList);

    List<QuestionnaireMetadataDocument> listModelToListDocument(List<QuestionnaireMetadataModel> modelList);

    @SuppressWarnings("removal")
    @AfterMapping
    default void fillModelAfterRead(
            QuestionnaireMetadataDocument doc,
            @MappingTarget QuestionnaireMetadataModel.QuestionnaireMetadataModelBuilder builder
    ) {
        if (doc.collectionInstrumentId() == null && doc.questionnaireId()!=null) {
            builder.collectionInstrumentId(doc.questionnaireId());
        }
    }
}
