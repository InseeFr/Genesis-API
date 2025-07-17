package fr.insee.genesis.domain.model.editedresponse;

import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import lombok.Builder;

import java.util.List;

@Builder
public record EditedResponseModel(
        String interrogationId,
        List<VariableQualityToolDto> editedPrevious,
        List<VariableQualityToolDto> editedExternal
){}
