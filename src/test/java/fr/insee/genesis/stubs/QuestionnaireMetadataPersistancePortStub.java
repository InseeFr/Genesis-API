package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
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
        remove(questionnaireMetadataModel.questionnaireId());
        mongoStub.add(QuestionnaireMetadataDocumentMapper.INSTANCE.modelToDocument(questionnaireMetadataModel));
    }

    @Override
    public QuestionnaireMetadataModel load(String questionnaireId) {
        List<QuestionnaireMetadataDocument> documents = mongoStub.stream().filter(
                        questionnaireMetadataDocument -> questionnaireMetadataDocument.questionnaireId().equals(questionnaireId))
                .toList();
        if (documents.isEmpty()) {
            return null;
        }
        return QuestionnaireMetadataDocumentMapper.INSTANCE.documentToModel(documents.getFirst());
    }

    @Override
    public void remove(String questionnaireId) {
        mongoStub.removeIf(questionnaireMetadataDocument -> questionnaireMetadataDocument.questionnaireId().equals(questionnaireId));
    }
}
