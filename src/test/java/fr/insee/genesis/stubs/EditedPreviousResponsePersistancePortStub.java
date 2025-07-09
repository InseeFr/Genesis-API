package fr.insee.genesis.stubs;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.editedprevious.EditedPreviousResponseModel;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.infrastructure.mappers.EditedPreviousResponseDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class EditedPreviousResponsePersistancePortStub implements EditedPreviousResponsePersistancePort {

    private final Map<String, List<EditedPreviousResponseDocument>> mongoStub; //<CollectionName, Collection>

    public EditedPreviousResponsePersistancePortStub() {
        mongoStub = new HashMap<>();
        mongoStub.put(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME, new ArrayList<>());
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
    public void saveAll(List<EditedPreviousResponseModel> editedPreviousResponseModelList) {
        mongoStub.put(
                Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME,
                EditedPreviousResponseDocumentMapper.INSTANCE.listModelToListDocument(editedPreviousResponseModelList));
    }

    @Override
    public void delete(String questionnaireId) {
        mongoStub.get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).removeIf(
                editedPreviousResponseDocument -> editedPreviousResponseDocument.getQuestionnaireId().equals(questionnaireId)
        );
    }
}
