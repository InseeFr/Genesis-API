package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

import java.util.List;

public interface QuestionnaireMetadataApiPort {
    MetadataModel find(String collectionInstrumentId, Mode mode) throws GenesisException;
    MetadataModel loadAndSaveIfNotExists(String campaignName, String collectionInstrumentId, Mode mode, FileUtils fileUtils,
                                         List<GenesisError> errors) throws GenesisException;
    void remove(String collectionInstrumentId, Mode mode);

    void save(String collectionInstrumentId, Mode mode, MetadataModel metadataModel);
}
