package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;

import java.util.List;

public interface EditedExternalResponsePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<EditedExternalResponseModel> editedPreviousResponseModelList);
    void delete(String questionnaireId);
    EditedExternalResponseDocument findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}