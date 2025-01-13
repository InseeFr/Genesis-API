package fr.insee.genesis.domain.model.surveymetadata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.surveymetadata.VariableDocument;
import lombok.Builder;

import java.util.Map;

@Builder
public record SurveyMetadataModel(
    String campaignId,
    String questionnaireId,
    Mode mode,
    Map<String, VariableDocument> variableDocumentMap
){}
