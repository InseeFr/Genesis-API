package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LastJsonExtractionPersistencePortStub implements LastJsonExtractionPersistencePort {

    List<LastJsonExtractionDocument> documents = new ArrayList<>();
    @Override
    public void save(LastJsonExtractionModel extraction) {

    }

    @Override
    public LastJsonExtractionModel getLastExecutionDate(String questionnaireModelId, Mode mode) throws GenesisException {
        return null;
    }
}
