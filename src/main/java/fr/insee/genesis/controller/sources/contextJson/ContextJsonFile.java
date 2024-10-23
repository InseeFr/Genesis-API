package fr.insee.genesis.controller.sources.contextJson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextJsonFile {

    @NotNull
    private String id;

    private String label;
    private String contexte; //household enterprise
    private ContextJsonMetadata metadonnees;
    private List<ContextJsonPartition> partitions;
    private List<ContextJsonQuestionnaireModel> questionnaireModels;
    private List<ContextJsonNomenclature> nomenclatures;


}
