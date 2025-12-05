package fr.insee.genesis.domain.model.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;

@Builder
public record QuestionnaireMetadataModel (
    String collectionInstrumentId,
    Mode mode,
    MetadataModel metadataModel
){}
