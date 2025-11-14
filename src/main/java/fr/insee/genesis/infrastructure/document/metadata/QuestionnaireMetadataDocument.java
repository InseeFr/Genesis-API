package fr.insee.genesis.infrastructure.document.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndex(name = "questionnaireId_1_mode_1", def = "{'questionnaireId': 1, 'mode': 1}")
@Document(collection = "questionnaireMetadatas")
public record QuestionnaireMetadataDocument(
        @Indexed
        String questionnaireId,
        Mode mode,
        MetadataModel metadataModel
){}
