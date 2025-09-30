package fr.insee.genesis.domain.service.extraction;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LastJsonExtractionApiPort;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LastJsonExtractionService implements LastJsonExtractionApiPort {

    @Qualifier("lastJsonExtractionMongoAdapter")
    LastJsonExtractionPersistencePort extractionPersistencePort;

    @Autowired
    public LastJsonExtractionService(LastJsonExtractionPersistencePort extractionPersistencePort) {
        this.extractionPersistencePort = extractionPersistencePort;
    }

    @Override
    public void recordDate(LastJsonExtractionModel extraction) {
        // Create a unique ID based on the questionnaire and the mode.
        extraction.setId(String.format("%s_%s",extraction.getQuestionnaireModelId(),extraction.getMode()));

        // save() does an insert if the id doesn't exist, otherwise an update
        extractionPersistencePort.save(extraction);
    }

    @Override
    public LastJsonExtractionModel getLastExtractionDate(String questionnaireModelId, Mode mode) throws GenesisException {
        return extractionPersistencePort.getLastExecutionDate(questionnaireModelId,mode);
    }


}
