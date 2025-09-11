package fr.insee.genesis.domain.service.extraction;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LastJsonExtractionApiPort;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LastJsonExtractionService implements LastJsonExtractionApiPort {

    LastJsonExtractionPersistencePort extractionPersistencePort;

    @Autowired
    public LastJsonExtractionService(LastJsonExtractionPersistencePort extractionPersistencePort) {
        this.extractionPersistencePort = extractionPersistencePort;
    }

    @Override
    public void recordDate(LastJsonExtractionModel extraction) {
        // Crée un id unique à partir du questionnaire et du mode
        extraction.setId(extraction.getQuestionnaireModelId() + "_" + extraction.getMode());

        // save() fait insert si id inexistant ou update sinon
        extractionPersistencePort.save(extraction);
    }

    @Override
    public LastJsonExtractionModel getLastExtractionDate(String questionnaireModelId, Mode mode) throws GenesisException {
        return extractionPersistencePort.getLastExecutionDate(questionnaireModelId,mode);
    }


}
