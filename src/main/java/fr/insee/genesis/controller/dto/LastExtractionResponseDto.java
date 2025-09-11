package fr.insee.genesis.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LastExtractionResponseDto {
    private final String lastExtractionDate;
    public LastExtractionResponseDto(LocalDateTime lastExtractionDate) {
        this.lastExtractionDate = lastExtractionDate != null ? lastExtractionDate.toString() : null;
    }
}
