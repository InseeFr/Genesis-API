package fr.insee.genesis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterrogationId {

	private String interrogationId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InterrogationId that = (InterrogationId) o;
		return Objects.equals(interrogationId, that.interrogationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(interrogationId);
	}
}
