package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterrogationBatchResponse {

    private List<InterrogationId> interrogationIds = new ArrayList<>();
    private Instant nextSince;

}
