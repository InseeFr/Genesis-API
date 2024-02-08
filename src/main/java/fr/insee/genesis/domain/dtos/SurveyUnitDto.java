package fr.insee.genesis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUnitDto {

	private String idUE;

	private Mode mode;

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
