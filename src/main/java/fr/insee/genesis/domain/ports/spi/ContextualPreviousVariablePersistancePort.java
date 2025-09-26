package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;

import java.util.List;

public interface ContextualPreviousVariablePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<ContextualPreviousVariableModel> contextualPreviousVariableModelList);
    void delete(String questionnaireId);
    ContextualPreviousVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}