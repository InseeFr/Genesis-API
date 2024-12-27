package fr.insee.genesis.domain.model.surveyunit;

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
public class SurveyUnitModel {

	private String idQuest;
	private String idCampaign;
	private String idUE;
	private DataState state;
	private Mode mode;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
	private LocalDateTime recordDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
	private LocalDateTime fileDate;

	private List<CollectedVariable> collectedVariables;
	private List<Variable> externalVariables;

	private String userIdentifier;

	public SurveyUnitModel(String idUE, Mode mode) {
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
		SurveyUnitModel that = (SurveyUnitModel) o;
		return Objects.equals(idUE, that.idUE) && Objects.equals(mode, that.mode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUE) + Objects.hash(mode);
	}
}
