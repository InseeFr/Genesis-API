package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisException;

public interface LastJsonExtractionPersistencePort {
    void save(LastJsonExtractionModel extraction);
    LastJsonExtractionModel getLastExecutionDate(String collectionInstrumentId, Mode mode) throws GenesisException;
}
