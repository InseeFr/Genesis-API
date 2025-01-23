package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class CollectedVariable extends Variable {

    private String loopId;
    private String parentId;

    @Builder(builderMethodName = "collectedVariableBuilder")
    public CollectedVariable(String varId, List<String> values, String loopId, String parentId) {
        super(varId, values);
        this.loopId = loopId;
        this.parentId = parentId;
    }

}
