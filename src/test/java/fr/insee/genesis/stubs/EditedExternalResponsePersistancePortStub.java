package fr.insee.genesis.stubs;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;
import fr.insee.genesis.domain.ports.spi.EditedExternalResponsePersistancePort;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import fr.insee.genesis.infrastructure.mappers.EditedExternalResponseDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class EditedExternalResponsePersistancePortStub implements EditedExternalResponsePersistancePort {

    private final Map<String, List<EditedExternalResponseDocument>> mongoStub; //<CollectionName, Collection>

    public EditedExternalResponsePersistancePortStub() {
        mongoStub = new HashMap<>();
        mongoStub.put(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME, new ArrayList<>());
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
    public void saveAll(List<EditedExternalResponseModel> editedExternalResponseModelList) {
        mongoStub.put(
                Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME,
                EditedExternalResponseDocumentMapper.INSTANCE.listModelToListDocument(editedExternalResponseModelList));
    }

    @Override
    public void delete(String questionnaireId) {
        mongoStub.get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).removeIf(
                editedExternalResponseDocument -> editedExternalResponseDocument.getQuestionnaireId().equals(questionnaireId)
        );
    }

    @Override
    public EditedExternalResponseDocument findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId) {
        List<EditedExternalResponseDocument> editedExternalResponseDocumentList = mongoStub.get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).stream().filter(
                editedExternalResponseDocument ->
                        editedExternalResponseDocument.getQuestionnaireId().equals(questionnaireId)
                                && editedExternalResponseDocument.getInterrogationId().equals(interrogationId)
        ).toList();
        return editedExternalResponseDocumentList.isEmpty() ? null : editedExternalResponseDocumentList.getFirst();
    }
}
