package fr.insee.genesis.stubs;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualExternalVariablePersistancePort;
import fr.insee.genesis.infrastructure.document.contextualexternal.ContextualExternalVariableDocument;
import fr.insee.genesis.infrastructure.mappers.ContextualExternalVariableDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ContextualExternalVariablePersistancePortStub implements ContextualExternalVariablePersistancePort {

    private final Map<String, List<ContextualExternalVariableDocument>> mongoStub; //<CollectionName, Collection>

    public ContextualExternalVariablePersistancePortStub() {
        mongoStub = new HashMap<>();
        mongoStub.put(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME, new ArrayList<>());
    }

    @Override
    public void backup(String questionnaireId) {
        if(mongoStub.containsKey(questionnaireId)) {
            mongoStub.put(questionnaireId + "_backup", new ArrayList<>(mongoStub.get(questionnaireId)));
        }
    }

    @Override
    public void deleteBackup(String questionnaireId) {
        mongoStub.remove(questionnaireId + "_backup");
    }

    @Override
    public void restoreBackup(String questionnaireId) {
        if(mongoStub.containsKey(questionnaireId + "_backup")) {
            delete(questionnaireId);
            mongoStub.put(questionnaireId, new ArrayList<>(mongoStub.get(questionnaireId + "_backup")));
        }
    }

    @Override
    public void saveAll(List<ContextualExternalVariableModel> contextualExternalVariableModelList) {
        mongoStub.put(
                Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME,
                ContextualExternalVariableDocumentMapper.INSTANCE.listModelToListDocument(contextualExternalVariableModelList));
    }

    @Override
    public void delete(String questionnaireId) {
        mongoStub.get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).removeIf(
                extDoc -> (extDoc.getQuestionnaireId() != null && extDoc.getQuestionnaireId().equals(questionnaireId)) ||
                            (extDoc.getCollectionInstrumentId() != null && extDoc.getCollectionInstrumentId().equals(questionnaireId))
        );
    }

    @Override
    public ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String questionnaireId, String interrogationId) {
        List<ContextualExternalVariableDocument> contextualExternalVariableDocumentList = mongoStub.get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).stream().filter(
                contextualExternalVariableDocument ->
                        contextualExternalVariableDocument.getQuestionnaireId().equals(questionnaireId)
                                && contextualExternalVariableDocument.getInterrogationId().equals(interrogationId)
        ).toList();
        return contextualExternalVariableDocumentList.isEmpty() ? null : ContextualExternalVariableDocumentMapper.INSTANCE.documentToModel(contextualExternalVariableDocumentList.getFirst());
    }

    @Override
    public Map<String, ContextualExternalVariableModel> findByCollectionInstrumentIdAndInterrogationIdList(String collectionInstrumentId, List<String> interrogationIds) {
        return Map.of();
    }
}
