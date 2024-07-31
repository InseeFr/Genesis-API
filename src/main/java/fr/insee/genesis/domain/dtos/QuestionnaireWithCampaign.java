package fr.insee.genesis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class QuestionnaireWithCampaign {
    private String idQuestionnaire;
    private Set<String> campaigns;
}
