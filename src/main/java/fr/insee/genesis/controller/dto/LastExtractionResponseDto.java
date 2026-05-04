package fr.insee.genesis.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class LastExtractionResponseDto {
    private final String lastExtractionDate;
    public LastExtractionResponseDto(Instant lastExtractionDate) {
        this.lastExtractionDate = lastExtractionDate != null ? lastExtractionDate.toString() : null;
    }
}
