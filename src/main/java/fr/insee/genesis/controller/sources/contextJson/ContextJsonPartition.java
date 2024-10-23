package fr.insee.genesis.controller.sources.contextJson;

import lombok.Data;

import java.util.List;

@Data
public class ContextJsonPartition {

    private int id;
    private String typeEchantillon;
    private String label;
    private String dateDebutCollecte;
    private String dateFinCollecte;
    private String dateRetour;
    private String questionnaireModel;
    private String quiRepond1;
    private String quiRepond2;
    private String quiRepond3;
    private List<Object> communications;

}
