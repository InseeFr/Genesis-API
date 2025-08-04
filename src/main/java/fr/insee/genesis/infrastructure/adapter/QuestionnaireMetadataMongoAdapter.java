package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistancePort;
import fr.insee.genesis.infrastructure.mappers.QuestionnaireMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.repository.QuestionnaireMetadataMongoDBRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Qualifier("questionnaireMetadataMongoAdapter")
public class QuestionnaireMetadataMongoAdapter implements QuestionnaireMetadataPersistancePort {
    private final QuestionnaireMetadataMongoDBRepository questionnaireMetadataMongoDBRepository;

    @Override
    @Cacheable(value = "metadatas")
    public List<QuestionnaireMetadataModel> find(String questionnaireId, Mode mode) {
        return QuestionnaireMetadataDocumentMapper.INSTANCE.listDocumentToListModel(
                questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(
                        questionnaireId, mode
                )
        );
    }

    @Override
    public void save(QuestionnaireMetadataModel questionnaireMetadataModel) {
        remove(questionnaireMetadataModel.questionnaireId(), questionnaireMetadataModel.mode());
        questionnaireMetadataMongoDBRepository.save(
                QuestionnaireMetadataDocumentMapper.INSTANCE.modelToDocument(questionnaireMetadataModel)
        );
    }

    @Override
    @CacheEvict(value = "metadatas")
    public void remove(String questionnaireId, Mode mode) {
        questionnaireMetadataMongoDBRepository.deleteByQuestionnaireIdAndMode(questionnaireId, mode);
    }
}
