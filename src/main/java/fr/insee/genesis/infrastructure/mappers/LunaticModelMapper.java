package fr.insee.genesis.infrastructure.mappers;
import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LunaticModelMapper {
    LunaticModelMapper INSTANCE = Mappers.getMapper(LunaticModelMapper.class);

    LunaticModelModel documentToModel(LunaticModelDocument document);

    LunaticModelDocument modelToDocument(LunaticModelModel model);

    List<LunaticModelModel> listDocumentToListModel(List<LunaticModelDocument> documentList);

    List<LunaticModelDocument> listModelToListDocument(List<LunaticModelModel> modelList);
}
