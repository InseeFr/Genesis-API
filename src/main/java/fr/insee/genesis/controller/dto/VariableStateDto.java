package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableStateDto {
    private DataState state;
    private boolean active;
    private Object value;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss.SSS",timezone = "UTC" )
    private Instant date;
}
