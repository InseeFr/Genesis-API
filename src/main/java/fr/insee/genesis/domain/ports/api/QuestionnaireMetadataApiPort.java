package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

import java.util.List;

public interface QuestionnaireMetadataApiPort {
    MetadataModel find(String questionnaireId, Mode mode) throws GenesisException;
    MetadataModel load(String campaignName, String questionnaireId, Mode mode, FileUtils fileUtils,
                       List<GenesisError> errors) throws GenesisException;
    void remove(String questionnaireId, Mode mode);

    void save(String questionnaireId, Mode mode, MetadataModel metadataModel);
}
