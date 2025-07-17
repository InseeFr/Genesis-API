package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.editedresponse.EditedResponseModel;

public interface EditedResponseApiPort {
    EditedResponseModel getEditedResponse(String questionnaireId, String interrogationId);
}