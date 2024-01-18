package fr.insee.genesis.domain.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SurveyUnitUpdateDto {

    private String idQuest;
    private String idCampaign;
    private String idUE;
    private DataState state;
    private Mode mode;
    private LocalDateTime recordDate;
    private LocalDateTime fileDate;
    private List<CollectedVariableDto> collectedVariables;
    private List<VariableDto> externalVariables;
}
