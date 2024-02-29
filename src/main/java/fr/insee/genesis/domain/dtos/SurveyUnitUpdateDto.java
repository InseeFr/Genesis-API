package fr.insee.genesis.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Mode mode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
    private LocalDateTime recordDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
    private LocalDateTime fileDate;
    private List<CollectedVariableDto> collectedVariables;
    private List<VariableDto> externalVariables;

}
