package fr.insee.genesis.infrastructure.document.surveyunit;

import lombok.Data;

import java.util.List;

@Data
public class GroupedInterrogationDocument {

    private String questionnaireId;
    private String partitionOrCampaignId;
    private List<String> interrogationIds;
}
