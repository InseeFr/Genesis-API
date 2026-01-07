package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;

import java.util.List;

public interface ContextualExternalVariablePersistancePort {
    void backup(String collectionInstrumentId);
    void deleteBackup(String collectionInstrumentId);
    void restoreBackup(String collectionInstrumentId);
    void saveAll(List<ContextualExternalVariableModel> contextualPreviousVariableModelList);
    void delete(String collectionInstrumentId);
    ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
}