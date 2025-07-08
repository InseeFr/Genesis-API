package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.editedprevious.EditedPreviousResponseModel;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.infrastructure.mappers.EditedPreviousResponseDocumentMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditedPreviousResponsePersistancePortStub implements EditedPreviousResponsePersistancePort {
    Map<String, List<EditedPreviousResponseDocument>> mongoStub = new HashMap<>();


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
        mongoStub.put(questionnaireId, new ArrayList<>(mongoStub.get(questionnaireId + "backup")));
    }

    @Override
    public void saveAll(String questionnaireId, List<EditedPreviousResponseModel> editedPreviousResponseModelList) {
        mongoStub.put(
                questionnaireId,
                EditedPreviousResponseDocumentMapper.INSTANCE.listModelToListDocument(editedPreviousResponseModelList));
    }

    @Override
    public void delete(String questionnaireId) {
        mongoStub.remove(questionnaireId);
    }
}
