package fr.insee.genesis.domain.model.contextualvariable;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ContextualPreviousVariableModel {
    String id;
    String questionnaireId;
    String interrogationId;
    Map<String,Object> variables;
    String sourceState;
}
