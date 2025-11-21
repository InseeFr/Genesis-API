package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;

import java.util.List;

public interface ContextualPreviousVariablePersistancePort {
    void backup(String collectionInstrumentId);
    void deleteBackup(String collectionInstrumentId);
    void restoreBackup(String collectionInstrumentId);
    void saveAll(List<ContextualPreviousVariableModel> contextualPreviousVariableModelList);
    void delete(String collectionInstrumentId);
    ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
}