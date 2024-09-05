package fr.insee.genesis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CampaignWithQuestionnaire {
    private String idCampaign;
    private Set<String> questionnaires;
}
