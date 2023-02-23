package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Builder
@Data
public class SurveyUnitUpdateDto {

    private String idQuest;
    private String idUE;
    private DataType type;
    private Date date;
    private List<VariableStateDto> variablesUpdate;

}
