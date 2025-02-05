package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VariableStateInputDto {

    private DataState state;

    private String value;

}
