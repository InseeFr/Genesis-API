package fr.insee.genesis.domain.model.contextualvariable;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ContextualExternalVariableModel {
    String id;
    String collectionInstrumentId;
    String interrogationId;
    Map<String,Object> variables;
}
