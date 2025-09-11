package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import fr.insee.genesis.infrastructure.mappers.LastJsonExtractionDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LastJsonExtractionMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Qualifier("lastJsonExtractionMongoAdapter")
@Slf4j
public class LastJsonExtractionMongoAdapter implements LastJsonExtractionPersistencePort {

    private final LastJsonExtractionMongoDBRepository extractionRepository;

    public LastJsonExtractionMongoAdapter(LastJsonExtractionMongoDBRepository extractionRepository) {
        this.extractionRepository = extractionRepository;
    }

    @Override
    public void save(LastJsonExtractionModel extraction) {
        extractionRepository.save(LastJsonExtractionDocumentMapper.INSTANCE.modelToDocument(extraction));
    }

    @Override
    public LastJsonExtractionModel getLastExecutionDate(String questionnaireModelId, Mode mode) throws GenesisException {
        String id = questionnaireModelId + "_" + mode;
        Optional<LastJsonExtractionDocument> extraction = extractionRepository.findById(id);
        if (extraction.isPresent()) {
            return LastJsonExtractionDocumentMapper.INSTANCE.documentToModel(extraction.get());
        } else {
            String message = String.format("No extraction date found for questionnaire %s and mode %s",questionnaireModelId,mode==null?null:mode.getModeName());
            throw new GenesisException(404,message);
        }
    }

}
