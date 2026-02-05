package integration_tests.stubs;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualPreviousVariablePersistancePort;
import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import fr.insee.genesis.infrastructure.mappers.ContextualPreviousVariableDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ContextualPreviousVariablePersistancePortStub implements ContextualPreviousVariablePersistancePort {

    private final Map<String, List<ContextualPreviousVariableDocument>> mongoStub; //<CollectionName, Collection>

    public ContextualPreviousVariablePersistancePortStub() {
        mongoStub = new HashMap<>();
        mongoStub.put(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME, new ArrayList<>());
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
    public void saveAll(List<ContextualPreviousVariableModel> contextualPreviousVariableModelList) {
        mongoStub.put(
                Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME,
                ContextualPreviousVariableDocumentMapper.INSTANCE.listModelToListDocument(contextualPreviousVariableModelList));
    }

    @Override
    public void delete(String questionnaireId) {
        mongoStub.get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).removeIf(
                previousDoc ->
                        (previousDoc.getQuestionnaireId()!=null && previousDoc.getQuestionnaireId().equals(questionnaireId)) ||
                                (previousDoc.getCollectionInstrumentId() != null && previousDoc.getCollectionInstrumentId().equals(questionnaireId))

        );
    }

    @Override
    public ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String questionnaireId, String interrogationId) {
        List<ContextualPreviousVariableDocument> contextualPreviousVariableDocumentList =
                mongoStub.get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).stream().filter(
                contextualPreviousVariableDocument ->
                    contextualPreviousVariableDocument.getQuestionnaireId().equals(questionnaireId)
                    && contextualPreviousVariableDocument.getInterrogationId().equals(interrogationId)
        ).toList();

        return contextualPreviousVariableDocumentList.isEmpty() ? null : ContextualPreviousVariableDocumentMapper.INSTANCE.documentToModel(contextualPreviousVariableDocumentList.getFirst());
    }
}
