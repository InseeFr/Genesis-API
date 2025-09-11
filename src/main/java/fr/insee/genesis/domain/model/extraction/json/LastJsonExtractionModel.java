package fr.insee.genesis.domain.model.extraction.json;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastJsonExtractionModel {
        @Id
        private String id; //Used to remove warning
        String questionnaireModelId;
        Mode mode;
        LocalDateTime lastExtractionDate;
}
