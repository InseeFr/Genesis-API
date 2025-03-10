package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.infrastructure.document.variabletype.VariableTypeDocument;
import org.springframework.cache.annotation.Cacheable;

public interface VariableTypePersistancePort {
    void save(VariableTypeModel variableTypeModel);
    @Cacheable("variableTypes")
    VariableTypeDocument find(String campaignId, String questionnaireId, Mode mode);
}
