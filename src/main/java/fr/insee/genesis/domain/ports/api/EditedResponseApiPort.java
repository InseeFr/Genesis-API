package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.editedresponse.EditedResponse;

public interface EditedResponseApiPort {
    EditedResponse getEditedResponse(String questionnaireId, String interrogationId);
}