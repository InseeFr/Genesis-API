package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;

import java.util.List;

public interface ContextualExternalVariablePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<ContextualExternalVariableModel> contextualPreviousVariableModelList);
    void delete(String questionnaireId);
    ContextualExternalVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}