package fr.insee.genesis.domain.model.surveyunit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class CampaignWithQuestionnaire {
    private String idCampaign;
    private Set<String> questionnaires;
}
