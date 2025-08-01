package fr.insee.genesis.domain.model.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;

public record QuestionnaireMetadataModel (
    String questionnaireId,
    Mode mode,
    MetadataModel metadataModel
){}
