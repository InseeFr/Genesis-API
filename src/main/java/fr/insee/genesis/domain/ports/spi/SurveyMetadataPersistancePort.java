package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import org.springframework.cache.annotation.Cacheable;

public interface SurveyMetadataPersistancePort {
    void save(SurveyMetadataModel surveyMetadataModel);
    @Cacheable("surveyMetadatas")
    SurveyMetadataDocument find(String campaignId, String questionnaireId, Mode mode);
}
