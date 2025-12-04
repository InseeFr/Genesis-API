package fr.insee.genesis.infrastructure.document.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/* It is not possible to respect these rules in a record (compilation error).
I choose to keep the record here (this choice can be challenged)*/
@SuppressWarnings({
        "java:S1123", // deprecated annotation usage
        "java:S6355"  // Add 'since' and/or 'forRemoval' arguments to the @Deprecated annotation
})
@CompoundIndex(name = "questionnaireId_1_mode_1", def = "{'questionnaireId': 1, 'mode': 1}")
@CompoundIndex(name = "collectionInstrumentId_1_mode_1", def = "{'collectionInstrumentId': 1, 'mode': 1}")
@Document(collection = "questionnaireMetadatas")
public record QuestionnaireMetadataDocument(

        @Deprecated
        @Indexed
        String questionnaireId,
        @Indexed
        String collectionInstrumentId,
        Mode mode,
        MetadataModel metadataModel
){}
