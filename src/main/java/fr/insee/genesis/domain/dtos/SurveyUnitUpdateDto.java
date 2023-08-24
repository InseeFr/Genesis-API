package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class SurveyUnitUpdateDto {

    private String idQuest;
    private String idCampaign;
    private String idUE;
    private DataState state;
    private Source source;
    private LocalDateTime date;
    private List<VariableStateDto> variablesUpdate;
    private List<ExternalVariableDto> externalVariables;

}
