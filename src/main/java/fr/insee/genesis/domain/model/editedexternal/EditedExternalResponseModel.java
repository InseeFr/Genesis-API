package fr.insee.genesis.domain.model.editedexternal;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class EditedExternalResponseModel {
    String id;
    String questionnaireId;
    String interrogationId;
    Map<String,Object> variables;
}
