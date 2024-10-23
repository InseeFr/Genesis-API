package fr.insee.genesis.controller.sources.contextJson;

import lombok.Data;

import java.util.List;

@Data
public class ContextJsonQuestionnaireModel {

    private String id;
    private String cheminRepertoire;
    private String label;
    private List<String> requiredNomenclatureIds;

}
