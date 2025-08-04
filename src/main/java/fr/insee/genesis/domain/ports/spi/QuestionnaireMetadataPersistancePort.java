package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;

import java.util.List;

public interface QuestionnaireMetadataPersistancePort {
    List<QuestionnaireMetadataModel> find(String questionnaireId, Mode mode);
    void save(QuestionnaireMetadataModel questionnaireMetadataModel);

    void remove(String questionnaireId, Mode mode);
}
