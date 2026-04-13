package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class VariableStateDto {
    private DataState state;
    private boolean active;
    private Object value;
    private Instant date;
}
