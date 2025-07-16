package fr.insee.genesis.domain.model.editedresponse.editedprevious;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class EditedPreviousResponseModel {
    String id;
    String questionnaireId;
    String interrogationId;
    Map<String,Object> variables;
    String sourceState;
}
