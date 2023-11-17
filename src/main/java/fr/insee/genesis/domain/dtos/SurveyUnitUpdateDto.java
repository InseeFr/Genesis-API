package fr.insee.genesis.domain.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<VariableStateDto> variablesUpdate;
    private List<ExternalVariableDto> externalVariables;

    /**
     * @return a copy of itself with forced status and record date set to .now()
     */
    public SurveyUnitUpdateDto buildForcedSurveyUnitUpdate() {
        return SurveyUnitUpdateDto.builder()
                .idQuest(idQuest)
                .idCampaign(idCampaign)
                .idUE(idUE)
                .state(DataState.FORCED)
                .mode(mode)
                .recordDate(LocalDateTime.now())
                .variablesUpdate(new ArrayList<>(variablesUpdate))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();
    }
}
