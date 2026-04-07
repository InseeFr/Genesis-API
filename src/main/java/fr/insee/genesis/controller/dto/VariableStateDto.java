package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class VariableStateDto {
    private DataState state;
    private boolean active;
    private Object value;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.VARIABLE_STATE_DTO_DATE_FORMAT)
    private LocalDateTime date;
}
