package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;

public interface QuestionnaireMetadataPersistancePort {
    void save(QuestionnaireMetadataModel questionnaireMetadataModel);
    QuestionnaireMetadataModel load(String questionnaireId, Mode mode);

    void remove(String questionnaireId, Mode mode);
}
