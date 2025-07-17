package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.editedresponse.EditedPreviousResponseModel;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;

import java.util.List;

public interface EditedPreviousResponsePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<EditedPreviousResponseModel> editedPreviousResponseModelList);
    void delete(String questionnaireId);
    EditedPreviousResponseDocument findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}