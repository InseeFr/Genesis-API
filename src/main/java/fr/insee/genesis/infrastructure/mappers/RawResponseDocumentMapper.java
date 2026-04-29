package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.controller.dto.rawdata.RawDataIdentifierDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface RawResponseDocumentMapper {

    RawResponseDocumentMapper INSTANCE = Mappers.getMapper(RawResponseDocumentMapper.class);

    RawResponseModel documentToModel(RawResponseDocument document);
    RawResponseDocument modelToDocument(RawResponseModel model);
    List<RawResponseModel> listDocumentToListModel(List<RawResponseDocument> documentList);
    List<RawResponseDocument> listModelToListDocument(List<RawResponseModel> modelList);

    @Mapping(
            target = "usualSurveyUnitId",
            expression = "java(extractUsualSurveyUnitId(document.payload()))"
    )
    RawDataIdentifierDto documentToRawDataIdentifierDto(RawResponseDocument document);

    // --- Custom mapping
    default String extractUsualSurveyUnitId(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Object value = payload.get("usualSurveyUnitId");
        return value != null ? value.toString() : null;
    }

    // --- Custom mapping: String -> Mode
    default Mode stringToMode(String value) {
        return Mode.fromString(value); // réutilise ton JsonCreator (parfait)
    }

    // --- Custom mapping: Mode -> String
    default String modeToString(Mode mode) {
        return mode != null ? mode.getJsonName() : null;
    }
}
