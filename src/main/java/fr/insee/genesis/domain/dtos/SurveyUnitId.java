package fr.insee.genesis.domain.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class SurveyUnitId {

	public SurveyUnitId(String idUE) {
		this.idUE = idUE;
	}

	public SurveyUnitId() {
	}

	@Getter
	@Setter
	@JsonProperty("idUE")
	private String idUE;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SurveyUnitId that = (SurveyUnitId) o;
		return Objects.equals(idUE, that.idUE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUE);
	}
}
