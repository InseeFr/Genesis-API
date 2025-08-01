package fr.insee.genesis.infrastructure.document.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@CompoundIndex(name = "questionnaireId_1_mode_1", def = "{'questionnaireId': 1, 'mode': 1}")
public record QuestionnaireMetadataDocument(
        String questionnaireId,
        Mode mode,
        MetadataModel metadataModel
){}
