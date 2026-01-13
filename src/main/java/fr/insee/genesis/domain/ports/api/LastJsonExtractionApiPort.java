package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisException;

public interface LastJsonExtractionApiPort {
    void recordDate(LastJsonExtractionModel extraction);

    LastJsonExtractionModel getLastExtractionDate(String collectionInstrumentId, Mode mode) throws GenesisException;

    void delete(String collectionInstrumentId, Mode mode) throws GenesisException;
}
