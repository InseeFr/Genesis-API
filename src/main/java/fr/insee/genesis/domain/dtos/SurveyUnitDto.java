package fr.insee.genesis.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUnitDto {

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

	public SurveyUnitDto(String idUE, Mode mode) {
		this.idUE = idUE;
		this.mode = mode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o){
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SurveyUnitDto that = (SurveyUnitDto) o;
		return Objects.equals(idUE, that.idUE) && Objects.equals(mode, that.mode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUE) + Objects.hash(mode);
	}
}
