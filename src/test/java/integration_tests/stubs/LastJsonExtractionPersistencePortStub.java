package integration_tests.stubs;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.LastJsonExtractionPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import fr.insee.genesis.infrastructure.mappers.LastJsonExtractionDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LastJsonExtractionPersistencePortStub implements LastJsonExtractionPersistencePort {

    List<LastJsonExtractionDocument> mongoStub = new ArrayList<>();
    @Override
    public void save(LastJsonExtractionModel extraction) {
        mongoStub.add(LastJsonExtractionDocumentMapper.INSTANCE.modelToDocument(extraction));
    }

    @Override
    public LastJsonExtractionModel getLastExecutionDate(String collectionInstrumentId, Mode mode) throws GenesisException {
        LastJsonExtractionDocument extraction = mongoStub.stream()
                .filter(doc -> doc.getId().equals(String.format("%s_%s",collectionInstrumentId,mode)))
                .findFirst()
                .orElseThrow();
        return LastJsonExtractionDocumentMapper.INSTANCE.documentToModel(extraction);
    }

    @Override
    public void delete(String collectionInstrumentId, Mode mode) {

    }
}
