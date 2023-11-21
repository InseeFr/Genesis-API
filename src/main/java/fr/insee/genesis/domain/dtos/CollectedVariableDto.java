package fr.insee.genesis.domain.dtos;

import lombok.*;

import java.util.List;


@Getter
@Setter
public class CollectedVariableDto extends VariableDto{

    private String idLoop;
    private String idParent;

    @Builder(builderMethodName = "collectedVariableBuilder")
    public CollectedVariableDto(String idVar, List<String> values, String idLoop, String idParent) {
        super(idVar, values);
        this.idLoop = idLoop;
        this.idParent = idParent;
    }

}
