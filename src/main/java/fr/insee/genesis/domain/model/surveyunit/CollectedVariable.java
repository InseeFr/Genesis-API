package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class CollectedVariable extends Variable {

    private String idLoop;
    private String idParent;

    @Builder(builderMethodName = "collectedVariableBuilder")
    public CollectedVariable(String idVar, List<String> values, String idLoop, String idParent) {
        super(idVar, values);
        this.idLoop = idLoop;
        this.idParent = idParent;
    }

}
