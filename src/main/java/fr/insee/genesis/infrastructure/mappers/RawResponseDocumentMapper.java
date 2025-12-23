package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RawResponseDocumentMapper {

    RawResponseDocumentMapper INSTANCE = Mappers.getMapper(RawResponseDocumentMapper.class);

    RawResponseModel documentToModel(RawResponseDocument document);
    RawResponseDocument modelToDocument(RawResponseModel model);
    List<RawResponseModel> listDocumentToListModel(List<RawResponseDocument> documentList);
    List<RawResponseDocument> listModelToListDocument(List<RawResponseModel> modelList);

    // --- Custom mapping: String -> Mode
    default Mode stringToMode(String value) {
        return Mode.fromString(value); // réutilise ton JsonCreator (parfait)
    }

    // --- Custom mapping: Mode -> String
    default String modeToString(Mode mode) {
        return mode != null ? mode.getJsonName() : null;
    }
}
