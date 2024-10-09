package fr.insee.genesis.controller.dto.perret;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class VariableStatePerret {
    private DataState state;
    private boolean active;
    private String value;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime date;
}
