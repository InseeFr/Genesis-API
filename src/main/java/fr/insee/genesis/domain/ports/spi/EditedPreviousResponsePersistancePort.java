package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.editedresponse.EditedPreviousResponseModel;

import java.util.List;

public interface EditedPreviousResponsePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<EditedPreviousResponseModel> editedPreviousResponseModelList);
    void delete(String questionnaireId);
    EditedPreviousResponseModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}