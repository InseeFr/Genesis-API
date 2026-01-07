package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LunaticModelMapper {
    LunaticModelMapper INSTANCE = Mappers.getMapper(LunaticModelMapper.class);

    LunaticModelModel documentToModel(LunaticModelDocument document);

    LunaticModelDocument modelToDocument(LunaticModelModel model);

    List<LunaticModelModel> listDocumentToListModel(List<LunaticModelDocument> documentList);

    List<LunaticModelDocument> listModelToListDocument(List<LunaticModelModel> modelList);

    @SuppressWarnings("removal")
    @AfterMapping
    default void fillModelAfterRead(
            LunaticModelDocument doc,
            @MappingTarget LunaticModelModel.LunaticModelModelBuilder builder
    ) {
        if (doc.collectionInstrumentId() == null && doc.questionnaireId()!=null) {
            builder.collectionInstrumentId(doc.questionnaireId());
        }
    }
}
