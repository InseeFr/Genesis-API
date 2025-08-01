package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistancePort;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.QuestionnaireMetadataDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestionnaireMetadataPersistancePortStub implements QuestionnaireMetadataPersistancePort {
    List<QuestionnaireMetadataDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(QuestionnaireMetadataModel questionnaireMetadataModel) {
        remove(questionnaireMetadataModel.questionnaireId(), questionnaireMetadataModel.mode());
        mongoStub.add(QuestionnaireMetadataDocumentMapper.INSTANCE.modelToDocument(questionnaireMetadataModel));
    }

    @Override
    public QuestionnaireMetadataModel load(String questionnaireId, Mode mode) {
        List<QuestionnaireMetadataDocument> documents = mongoStub.stream().filter(
                        questionnaireMetadataDocument -> questionnaireMetadataDocument.questionnaireId().equals(questionnaireId)
                && questionnaireMetadataDocument.mode().equals(mode))
                .toList();
        if (documents.isEmpty()) {
            return null;
        }
        return QuestionnaireMetadataDocumentMapper.INSTANCE.documentToModel(documents.getFirst());
    }

    @Override
    public void remove(String questionnaireId, Mode mode) {
        mongoStub.removeIf(questionnaireMetadataDocument -> questionnaireMetadataDocument.questionnaireId().equals(questionnaireId)
            && questionnaireMetadataDocument.mode().equals(mode)
        );
    }
}
