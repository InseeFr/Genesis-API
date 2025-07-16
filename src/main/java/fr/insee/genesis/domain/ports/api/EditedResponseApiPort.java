package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.EditedResponseDto;

public interface EditedResponseApiPort {
    EditedResponseDto getEditedResponse(String questionnaireId, String interrogationId);
}