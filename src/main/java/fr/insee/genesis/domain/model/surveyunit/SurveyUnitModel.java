package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/*
 * This model class should be in equation with the model of our information system ("modèle filière)
 * Its up to the adapter and mappers to deal with old fields
 *
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUnitModel {


	private String questionnaireId;
	private String campaignId;
	private String interrogationId;
	private String idUE;
	private DataState state;
	private Mode mode;
	private String contextualId;
	private Boolean isCapturedIndirectly;
	private LocalDateTime validationDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
	private LocalDateTime recordDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm")
	private LocalDateTime fileDate;

	private List<VariableModel> collectedVariables;
	private List<VariableModel> externalVariables;

	private String modifiedBy;

	public SurveyUnitModel(String interrogationId, Mode mode) {
		this.interrogationId = interrogationId;
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
		return Objects.equals(interrogationId, that.interrogationId) && Objects.equals(mode, that.mode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(interrogationId) + Objects.hash(mode);
	}
}
