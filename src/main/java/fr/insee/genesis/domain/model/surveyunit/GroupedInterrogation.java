package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;

import java.util.List;


@Builder
public record GroupedInterrogation(String questionnaireId, String partitionOrCampaignId, List<String> interrogationIds) {}
