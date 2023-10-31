package fr.insee.genesis.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
public class SurveyUnitId {

	@Getter
	@Setter
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
