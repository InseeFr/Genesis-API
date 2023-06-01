package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VariableStateDto {

    private String idVar;
    private DataType type;
    private String idLoop;
    private String idParent;
    private List<String> values;

}
