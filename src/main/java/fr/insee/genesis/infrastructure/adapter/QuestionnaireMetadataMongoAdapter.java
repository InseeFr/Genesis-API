package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.QuestionnaireMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.repository.QuestionnaireMetadataMongoDBRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Qualifier("questionnaireMetadataMongoAdapter")
public class QuestionnaireMetadataMongoAdapter implements QuestionnaireMetadataPersistencePort {
    private final QuestionnaireMetadataMongoDBRepository questionnaireMetadataMongoDBRepository;

    @Override
    @Cacheable(value = "metadatas", key = "#collectionInstrumentId + '-' + #mode")
    public List<QuestionnaireMetadataModel> find(String collectionInstrumentId, Mode mode) {
        List<QuestionnaireMetadataDocument> results = new ArrayList<>();
        results.addAll(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(collectionInstrumentId, mode));
        results.addAll(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(collectionInstrumentId, mode));
        return QuestionnaireMetadataDocumentMapper.INSTANCE.listDocumentToListModel(results);
    }

    @Override
    @CacheEvict(value = "metadatas", key = "#questionnaireMetadataModel.collectionInstrumentId() + '-' + #questionnaireMetadataModel.mode()")
    public void save(QuestionnaireMetadataModel questionnaireMetadataModel) {
        remove(questionnaireMetadataModel.collectionInstrumentId(), questionnaireMetadataModel.mode());
        questionnaireMetadataMongoDBRepository.save(
                QuestionnaireMetadataDocumentMapper.INSTANCE.modelToDocument(questionnaireMetadataModel)
        );
    }

    @Override
    @CacheEvict(value = "metadatas", key = "#collectionInstrumentId + '-' + #mode")
    public void remove(String collectionInstrumentId, Mode mode) {
        questionnaireMetadataMongoDBRepository.deleteByQuestionnaireIdAndMode(collectionInstrumentId, mode);
        questionnaireMetadataMongoDBRepository.deleteByCollectionInstrumentIdIdAndMode(collectionInstrumentId, mode);
    }
}
