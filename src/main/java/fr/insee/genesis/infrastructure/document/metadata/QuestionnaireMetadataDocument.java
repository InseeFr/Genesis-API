package fr.insee.genesis.infrastructure.document.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import org.springframework.data.mongodb.core.index.Indexed;

public record QuestionnaireMetadataDocument(
        @Indexed
        String questionnaireId,
        MetadataModel metadataModel
){}
