package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VariableStateInputDto {

    private DataState state;

    private Object value;

}
