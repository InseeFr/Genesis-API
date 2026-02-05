package integration_tests.stubs;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.QuestionnaireMetadataDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestionnaireMetadataPersistencePortStub implements QuestionnaireMetadataPersistencePort {
    List<QuestionnaireMetadataDocument> mongoStub = new ArrayList<>();

    @Override
    public List<QuestionnaireMetadataModel> find(String collectionInstrumentId, Mode mode) {
        return QuestionnaireMetadataDocumentMapper.INSTANCE.listDocumentToListModel(mongoStub.stream().filter(
                        questionnaireMetadataDocument -> (questionnaireMetadataDocument.questionnaireId()!= null && questionnaireMetadataDocument.questionnaireId().equals(collectionInstrumentId) || questionnaireMetadataDocument.collectionInstrumentId()!= null && questionnaireMetadataDocument.collectionInstrumentId().equals(collectionInstrumentId))
                                && questionnaireMetadataDocument.mode().equals(mode))
                .toList());
    }

    @Override
    public void save(QuestionnaireMetadataModel questionnaireMetadataModel) {
        remove(questionnaireMetadataModel.collectionInstrumentId(), questionnaireMetadataModel.mode());
        mongoStub.add(QuestionnaireMetadataDocumentMapper.INSTANCE.modelToDocument(questionnaireMetadataModel));
    }

    @Override
    public void remove(String collectionInstrumentId, Mode mode) {
        mongoStub.removeIf(questionnaireMetadataDocument -> questionnaireMetadataDocument.collectionInstrumentId()!= null && questionnaireMetadataDocument.collectionInstrumentId().equals(collectionInstrumentId)
            && questionnaireMetadataDocument.mode().equals(mode)
        );
        mongoStub.removeIf(questionnaireMetadataDocument -> questionnaireMetadataDocument.questionnaireId()!= null && questionnaireMetadataDocument.questionnaireId().equals(collectionInstrumentId)
                && questionnaireMetadataDocument.mode().equals(mode)
        );
    }
}
