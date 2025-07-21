package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;

import java.util.List;

public interface EditedExternalResponsePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<EditedExternalResponseModel> editedPreviousResponseModelList);
    void delete(String questionnaireId);
    EditedExternalResponseModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}