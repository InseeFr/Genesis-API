package fr.insee.genesis.domain.model.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;

public record QuestionnaireMetadataModel (
    String questionnaireId,
    MetadataModel metadataModel
){}
