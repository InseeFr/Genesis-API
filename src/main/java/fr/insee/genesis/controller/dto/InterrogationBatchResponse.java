package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class InterrogationBatchResponse {

    private List<InterrogationId> interrogationIds = new ArrayList<>();
    private Instant nextSince;

}
