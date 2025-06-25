package fr.insee.genesis.domain.model.surveyunit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupedInterrogation {

    private String questionnaireId;
    private String partitionOrCampaignId;
    private List<String> interrogationIds;

}
