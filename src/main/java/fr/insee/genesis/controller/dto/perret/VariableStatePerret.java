package fr.insee.genesis.controller.dto.perret;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class VariableStatePerret {
    private DataState state;
    private boolean active;
    private String value;
    private LocalDate date;
}
