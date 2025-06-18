package fr.insee.genesis.infrastructure.document.surveyunit;

import lombok.Data;

import java.util.List;

@Data
public class GroupedInterrogationDocument {

    private String questionnaireId;
    private List<String> interrogationIds;
}
