package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.editedexternal.EditedExternalResponseModel;

import java.util.List;

public interface EditedExternalResponsePersistancePort {
    void backup(String questionnaireId);
    void deleteBackup(String questionnaireId);
    void restoreBackup(String questionnaireId);
    void saveAll(List<EditedExternalResponseModel> editedPreviousResponseModelList);
    void delete(String questionnaireId);
}