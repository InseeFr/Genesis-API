package fr.insee.genesis.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class LastExtractionRequest {

    @NotNull
    private Instant lastExtractionDate;

}
