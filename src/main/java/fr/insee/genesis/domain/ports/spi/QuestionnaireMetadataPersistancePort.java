package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;

public interface QuestionnaireMetadataPersistancePort {
    void save(QuestionnaireMetadataModel questionnaireMetadataModel);
    QuestionnaireMetadataModel load(String questionnaireId);

    void remove(String questionnaireId);
}
