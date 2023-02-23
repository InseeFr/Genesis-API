package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VariableStateDto {

    private String idParentRow;
    private String idLoopRow;
    private String idVar;
    private List<String> values;

}
