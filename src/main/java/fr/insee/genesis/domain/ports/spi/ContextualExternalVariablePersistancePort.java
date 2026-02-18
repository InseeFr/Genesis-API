package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;

import java.util.List;
import java.util.Map;

public interface ContextualExternalVariablePersistancePort {
    void backup(String collectionInstrumentId);
    void deleteBackup(String collectionInstrumentId);
    void restoreBackup(String collectionInstrumentId);
    void saveAll(List<ContextualExternalVariableModel> contextualPreviousVariableModelList);
    void delete(String collectionInstrumentId);
    ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
    Map<String, ContextualExternalVariableModel> findByCollectionInstrumentIdAndInterrogationIdList(String collectionInstrumentId, List<String> interrogationIds);
}