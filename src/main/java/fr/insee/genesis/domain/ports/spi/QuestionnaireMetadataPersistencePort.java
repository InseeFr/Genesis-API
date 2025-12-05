package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;

import java.util.List;

public interface QuestionnaireMetadataPersistencePort {
    List<QuestionnaireMetadataModel> find(String collectionInstrumentId, Mode mode);
    void save(QuestionnaireMetadataModel questionnaireMetadataModel);

    void remove(String collectionInstrumentId, Mode mode);
}
